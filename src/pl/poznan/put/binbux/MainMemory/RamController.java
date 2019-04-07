package pl.poznan.put.binbux.MainMemory;

import pl.poznan.put.binbux.HardDisk.INode;
import pl.poznan.put.binbux.Kernel;
import pl.poznan.put.binbux.ProcessModule.ProcessNode;

import java.util.Hashtable;
import java.util.Vector;

public class RamController {
    private Ram memory = new Ram();
    private Hashtable<Integer, Character> framesData = new Hashtable<>();

    //'f' -> free frame
    //'o' -> occupied frame

    public RamController() {
        for (int i = 0; i < memory.size / memory.frameSize; i++) {
            framesData.put(i, 'f');
        }
    }

    private int countFreeSpace(){
        int counter = 0;
        for(char value : framesData.values()){
            if(value == 'f') counter++;
        }
        return counter*memory.frameSize;
    }

    private int getFirstFreeFrame(){
        for(int i=0;i<framesData.size();i++){
            if(framesData.get(i).equals('f')) {
                framesData.put(i,'o');
                return i;
            }
        }
        throw new OutOfMemoryError("No free frames in memory");
    }

    private int frameAddress(int frameNum){
        return frameNum*memory.frameSize;
    }

    private int convert(int logicalAddress, ProcessNode process) {
        int page = logicalAddress / memory.frameSize;
        int offset = logicalAddress % memory.frameSize;
        return frameAddress(process.getPageTable().get(page)) + offset;
    }

    public void write(int logicalAddr, char[] values, ProcessNode process) {
        for(int i=0;i<values.length;i++) {
            memory.write(convert(logicalAddr+i, process), values[i]);
        }
    }

    public char[] read(int logicalAddr,int length, ProcessNode process) {
        char[] buffer = new char[length];
        for(int i=0;i<length;i++) {
            buffer[i] =  memory.read(convert(logicalAddr+i, process));
        }
        return buffer;
    }

    public String readInstruction(int logicAddress, ProcessNode process) {

        StringBuilder instructionBuild = new StringBuilder();
        char current;
        int i = 0;
        do {
            current = memory.read(convert(logicAddress,process) + i);
            if (current != ';') instructionBuild.append(current);
            i++;
        } while (current != ';');
        return instructionBuild.toString();
    }

    public Vector<Integer> load(INode file,ProcessNode process) {
        char[] buffer = Kernel.disk.readFile(file,0,file.size,process);
        String program = new String(buffer);
        program = program.replace("\n","").replace("\r", "");
        return load(program,process);
    }

    public Vector<Integer> load(String program,ProcessNode process) {
        if(program.length()<= countFreeSpace()){
            Vector<Integer> newPageTable = new Vector<>();
            int currentFrame=-1;
            for(int i=0;i<program.length();i++){
                if(i%memory.frameSize == 0) {
                    currentFrame = getFirstFreeFrame();
                    newPageTable.add(currentFrame);
                }
                memory.write(frameAddress(currentFrame)+i%memory.frameSize,program.charAt(i));
            }
            process.setLength(program.length());
            return newPageTable;
        }
        else throw new OutOfMemoryError("wanted"+ program.length() +" bytes, free "+countFreeSpace()+" bytes");
    }

    public void free(ProcessNode process) {
        for(int frame : process.getPageTable()){
            framesData.put(frame,'f');
        }
    }
    public void ramStatus(){
        Kernel.shell.stdout("Ramka\tstatus");
        for(int i=0;i<framesData.size();i++){
            Kernel.shell.stdout(i+"\t\t"+framesData.get(i));
        }
    }
}
