package pl.poznan.put.binbux.HardDisk;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import pl.poznan.put.binbux.Kernel;
import pl.poznan.put.binbux.ProcessModule.ProcessNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Vector;

public class DiskController {
    private Disk hdd;
    private INode[] inodeTable;
    private Hashtable<String, Integer> rootFolder;

    static String readFile(String path) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded);
    }
    static void saveFile(String path, String contents) throws IOException
    {
        Files.write(Paths.get(path), contents.getBytes());
    }

    public DiskController() {

        if(Files.exists(Paths.get("disk.vhd"))) {
            //deserializacjadgksn
            try {
                hdd = new Gson().fromJson(readFile("disk.vhd"), Disk.class);
                inodeTable = new Gson().fromJson(readFile("inodes.vhd"), INode[].class);
                rootFolder = new Gson().fromJson(readFile("rootfolder.vhd"), new TypeToken<Hashtable<String, Integer>>(){}.getType());
            } catch (Throwable ex) {
                Kernel.shell.stdout("Błąd podczas wczytywania dysku! Tworzenie nowego. ");
                ex.printStackTrace();
                hdd = new Disk();
                inodeTable = new INode[hdd.size/hdd.blockSize];
                rootFolder = new Hashtable<>();
            }
        } else {
            hdd = new Disk();
            inodeTable = new INode[hdd.size/hdd.blockSize];
            rootFolder = new Hashtable<>();
        }
    }
    public void SaveVHD() {
        try {
            saveFile("disk.vhd", new Gson().toJson(hdd));
            saveFile("inodes.vhd", new Gson().toJson(inodeTable));
            saveFile("rootfolder.vhd", new Gson().toJson(rootFolder));
        } catch (Throwable ex) {
            Kernel.shell.stdout("Błąd podczas zapisywania dysku!.");
        }
    }

    private int getFreeBlocks() {
        int free = hdd.size/hdd.blockSize;
        for (INode node : inodeTable) {
            if (node != null) free -= blocksCount(node.size);
        }
        return free;
    }

    private int convert(int logicalAddress, INode file) {
        int offset = logicalAddress % hdd.blockSize;
        int blockNum = logicalAddress / hdd.blockSize;
        if (blockNum == 0) {
            return addressFromBlock(file.directBlock1) + offset;
        } else if (blockNum == 1) {
            return addressFromBlock(file.directBlock2) + offset;
        } else if (blockNum >= 2) {
            int block = hdd.read((addressFromBlock(file.indirectBlock)) + blockNum - 2);
            return addressFromBlock(block) + offset;
        }
        return -1;
    }

    private int addressFromBlock(int blockNum) {
        return blockNum * hdd.blockSize;
    }

    private int firstFreeINode() {
        for (int i = 0; i < inodeTable.length; i++) {
            if (inodeTable[i] == null) return i;
        }
        throw new OutOfMemoryError("Osiągnięto maksymalną ilość plików");
    }

    private int firstFreeBlock() {
        Vector<Integer> blocks = new Vector<>();
        for (int i = 0; i < hdd.size/hdd.blockSize; i++) {
            blocks.add(i);
        }
        for (INode node : inodeTable) {
            if (node != null) {
                blocks.remove((Integer) node.directBlock1);
                if (node.directBlock2 != -1) {
                    blocks.remove((Integer) node.directBlock2);

                }
                if (node.indirectBlock != -1) {
                    int i = 0;
                    while (hdd.read(addressFromBlock(node.indirectBlock) + i) != 255) {
                        blocks.remove((Integer) (int) hdd.read(addressFromBlock(node.indirectBlock) + i));
                        i++;
                    }
                    blocks.remove((Integer) node.indirectBlock);
                }
            }
        }
        if (blocks.size() == 0) {
            throw new OutOfMemoryError("Brak miejsca na dysku twardym");
        }
        return blocks.get(0);
    }

    private int blocksCount(int size) {
        if (size == 0) return 1;
        return (int) Math.ceil((float) size / (float) hdd.blockSize);
    }

    public INode createFile(String name, int attrib) throws FileAlreadyExistsException {
        if (!rootFolder.containsKey(name)) {
            INode newNode = new INode(firstFreeBlock(), attrib);
            int index = firstFreeINode();
            inodeTable[index] = newNode;
            rootFolder.put(name, index);
            Kernel.shell.log("dysku","Utworzono plik o nazwie \""+name+"\"");
            return newNode;
        } else {
            throw new FileAlreadyExistsException("Plik o takiej nazwie istnieje już na dysku");
        }
    }

    public void writeFile(INode file, char[] buffer, int length, int pos, ProcessNode process) {
        Kernel.syncer.lock(6, process);
        if (pos + length > file.size) {
            resizeFile(file, pos + length);
        }
        for (int i = 0; i < length; i++, pos++) {
            hdd.write(convert(pos, file), buffer[i]);
        }
        Kernel.shell.log("dysku","Zapisano "+length+" bajtów do pliku");
        Kernel.syncer.broadcast(6);
        Kernel.syncer.unlock(6, process);
    }

    public char[] readFile(INode file, int pos, int length, ProcessNode process) {
        if (Kernel.syncer.tryLock(6, process)) {
            Kernel.syncer.unlock(6, process);
            char[] buffer = new char[length];
            for (int i = 0; i < length; i++, pos++) {
                buffer[i] = hdd.read(convert(pos, file));
            }
            return buffer;
        } else {
            Kernel.syncer.wait(6, process);
            return new char[0];
        }
    }

    public void loadFile(String name, String path, int attrib) throws IOException {
        File file = new File(path);
        byte[] buffer = new byte[(int) file.length()];
        FileInputStream IS = new FileInputStream(file);

        int len = IS.read(buffer, 0, (int) file.length());
        if (len != file.length())
            throw new IOException("Błąd odczytu pliku " + file.getAbsolutePath() + "\nBajtów: " + file.length() + " odczytano: " + len);

        String data = new String(buffer);
        try {
            INode newFile = createFile(name, attrib);
            writeFile(newFile, data.toCharArray(), data.length(), 0, Kernel.initProc);
        } catch (Throwable e) {
            Kernel.processManager.err(e.getMessage());
        }
    }

    public INode getFile(String name) throws FileNotFoundException {
        try {
            return inodeTable[rootFolder.get(name)];
        } catch(Throwable ex) {
            throw new FileNotFoundException("Nie istnieje taki plik na dysku");
        }
    }

    public void resizeFile(INode file, int size) {
        int difference = size - file.size;
        int blocksNeeded = blocksCount(difference);
        if (blocksNeeded > 0) blocksNeeded--;
        if (blocksNeeded <= getFreeBlocks()) {
            while (blocksNeeded != 0) {
                if (blocksNeeded > 0) {
                    if (file.directBlock2 == -1) {
                        file.directBlock2 = firstFreeBlock();
                    } else {
                        if (file.indirectBlock == -1) {
                            file.indirectBlock = firstFreeBlock();
                            for (int i = 0; i < hdd.blockSize; i++) {
                                hdd.write(addressFromBlock(file.indirectBlock) + i, (char) 255);
                            }
                        }
                        char[] indirects = new char[hdd.blockSize];
                        for (int i = 0; i < hdd.blockSize; i++) {
                            indirects[i] = hdd.read(addressFromBlock(file.indirectBlock) + i);
                        }

                        for (int i = 0; i < hdd.blockSize; i++) {
                            if (indirects[i] == 255) {
                                hdd.write(addressFromBlock(file.indirectBlock) + i, (char) firstFreeBlock());
                                break;
                            }
                        }
                    }
                    blocksNeeded--;
                }
                if (blocksNeeded < 0) {
                    if (file.indirectBlock != -1) {
                        char[] indirects = new char[hdd.blockSize];
                        for (int i = 0; i < hdd.blockSize; i++) {
                            indirects[i] = hdd.read(addressFromBlock(file.indirectBlock) + i);
                        }
                        boolean test = true;
                        for (int i = hdd.blockSize - 1; i >= 0; i--) {
                            if (indirects[i] == 255) continue;
                            hdd.write(addressFromBlock(file.indirectBlock) + i, (char) 255);
                            test = (i==0);
                            break;
                        }
                        if (test) {
                            file.indirectBlock = -1;
                        }
                    } else if (file.directBlock2 != -1) {
                        file.directBlock2 = -1;
                    }
                    blocksNeeded++;
                }

            }
            Kernel.shell.log("dysku","Zmieniono rozmiar pliku z "+file.size+" na "+file.size+difference);
            file.size += difference;
        } else throw new OutOfMemoryError("Brak miejsca na dysku twardym");
    }

    public void deleteFile(String name) {
        if (rootFolder.containsKey(name)) {
            Integer index = rootFolder.get(name);
            inodeTable[index] = null;
            rootFolder.remove(name);
            Kernel.shell.log("dysku","Usunięto plik "+name);
        } else
            Kernel.shell.stdout("Nie ma takiego pliku");
    }

    public void printFiles() {
        for (String file : rootFolder.keySet()) {
            Kernel.shell.stdout(file + " ", true);
        }
        //Kernel.shell.stdout();
    }

    private String attributeDecode(int code) {
        switch (code) {
            case 0:
                return "---";
            case 1:
                return "--x";
            case 2:
                return "-w-";
            case 3:
                return "-wx";
            case 4:
                return "r--";
            case 5:
                return "r-x";
            case 6:
                return "rw-";
            case 7:
                return "rwx";
            default:
                return null;
        }
    }

    private String getVisualAttributes(int value) {
        int first = value / 64;
        int second = value / 8 - first * 8;
        int third = value - (first * 64 + second * 8);
        return attributeDecode(first) + attributeDecode(second) + attributeDecode(third);
    }

    public void printFilesList() {
        Kernel.shell.stdout("Wszystkich plików: " + rootFolder.size());
        for (String file : rootFolder.keySet()) {
            Kernel.shell.stdout(getVisualAttributes(inodeTable[rootFolder.get(file)].attributes) + "\t", true);
            Kernel.shell.stdout("root root\t", true);
            Kernel.shell.stdout(inodeTable[rootFolder.get(file)].size + "\t", true);
            Kernel.shell.stdout(file + "\n", true);
        }
    }

    public void printDisk() {
        for (int i = 0; i < hdd.size; i++) {
            if (i % 32 == 0)
                Kernel.shell.stdout("\n" + i + ": ", true);
            if (hdd.read(i) != '\n')
                Kernel.shell.stdout((byte)hdd.read(i) + "|", true);
            else
                Kernel.shell.stdout("\\n|", true);
        }
        //Kernel.shell.stdout();
    }

}
