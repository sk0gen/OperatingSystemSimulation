package pl.poznan.put.binbux.Shell;

import pl.poznan.put.binbux.HardDisk.INode;
import pl.poznan.put.binbux.Kernel;
import pl.poznan.put.binbux.ProcessModule.ProcessNode;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class Shell {
    private ShellController controller;
    public boolean autoStep = true;
    public Shell(ShellController _controller) {
        controller = _controller;
        stdout("BinBux v2.1.3.7 OS\nTest Shell for debug purposes.\nBinBux © 2018 - 2019");
    }
    public void stdout(String message) {
        String line = message+"\r\n";
        javafx.application.Platform.runLater( () -> controller.stdout.appendText(line) );
        logToFile(line, true);
    }
    public void stdout(String message, boolean skipNL) {
        String line = message;
        javafx.application.Platform.runLater( () -> controller.stdout.appendText(line) );
        logToFile(line, true);
    }
    public void log(String module, String message) {
        String line = "[Moduł " + module + "] "+ message + "\r\n";
        javafx.application.Platform.runLater( () -> controller.logout.appendText(line) );
        logToFile(line, false);
    }
    private void logToFile(String line, Boolean std) {
        String fileName = "stdout.txt";
        if(!std) {
            fileName = "log.txt";
        }
        try(PrintWriter output = new PrintWriter(new FileWriter(fileName,true)))
        {
            output.printf("%s", line);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    public void ParseCommand(String polecenie) {
        String[] commands = polecenie.split(" ");
        switch (commands[0]) {
            case "run": {
                if (commands.length == 3) {
                    String name = commands[1];
                    int parentid = Integer.parseInt(commands[2]);
                    Kernel.processManager.createProcess(Kernel.processManager.getProcessByID(parentid), name, "");
                } else if (commands.length == 2) {
                    String name = commands[1];
                    Kernel.processManager.createProcess(Kernel.initProc, name, "");
                } else if (commands.length >= 4) {
                    String name = commands[1];
                    int parentid = Integer.parseInt(commands[2]);
                    String splice = (String.join(" ", Arrays.copyOfRange(commands, 3, commands.length)));
                    Kernel.processManager.createProcess(Kernel.processManager.getProcessByID(parentid), name, splice);
                } else Kernel.processManager.createProcess(Kernel.initProc, "NowyProces", "");
            }
            break;
            case "kill": {
                int pid;
                if (commands.length == 2) {
                    if (commands[1].equals("-a")) {
                        for (ProcessNode process : Kernel.processManager.getAllProcesses()) {
                            if (process.getPID() != 1) {
                                Kernel.cpu.returnProcess(process);
                                Kernel.processManager.err(process,-1);
                            }
                        }
                    } else {
                        pid = Integer.parseInt(commands[1]);
                        ProcessNode process = Kernel.processManager.getProcessByID(pid);
                        Kernel.cpu.returnProcess(process);
                        Kernel.processManager.err(process,-1);
                    }
                } else {
                    Kernel.shell.stdout("Musisz podać numer procesu");
                }
            }
            break;
            case "ps": {
                ProcessNode init = Kernel.initProc;
                Kernel.shell.stdout("PID\t\tNazwa\t\tPPID\t\tStan");
                Kernel.shell.stdout(init.getPID() + "\t\t" + init.getName() + "\t\tbrak\t\t");
                for (ProcessNode proc : Kernel.processManager.getAllProcesses()) {
                    Kernel.shell.stdout(proc.getPID() + "\t\t" + proc.getName() + "\t\t" + proc.getParent().getPID() + "\t\t" + proc.getState());

                }
            }
            break;
            case "tree":
                recursivePrint(Kernel.initProc, 0);
                break;
            case "testLock": {
                String prog1 = "HLW wchodzeDoZamka1;MOV A 50;LCK 1;DEC A;HLW siedzeWZamku1;JNZ 35;UNL 1;HLW wyszedlem1;RET 0;";
                String prog2 = "HLW wchodzeDoZamka2;MOV A 50;LCK 1;DEC A;HLW siedzeWZamku2;JNZ 35;UNL 1;HLW wyszedlem2;RET 0;";
                Kernel.processManager.createProcess(Kernel.initProc, "proces1", prog1);
                Kernel.processManager.createProcess(Kernel.initProc, "proces2", prog2);
                break;
            }
            case "testCond": {
                String prog1 = "HLW CzekamNaWarunek1;WAT 1;HLW Przeszedłem1;RET 0;";
                String prog2 = "HLW Start2;MOV A 50;DEC A;JNZ 20;HLW idzieSygnał2;SIG 1;RET 0;";
                Kernel.processManager.createProcess(Kernel.initProc, "process1", prog1);
                Kernel.processManager.createProcess(Kernel.initProc, "process2", prog2);
                break;
            }

            case "testPipes": {
                String prog1 = "HLW p1;COP pip1 0;SMP pip1 1;SMP pip1 2;SMP pip1 3;SMP pip1 4;SMP pip1 5;SMP pip1 6;SMP pip1 7;WAT 5;DOP pip1;HLW o1;RET 0;";
                String prog2 = "HLW p2;RMP pip1;RMP pip1;RMP pip1;RMP pip1;RMP pip1;RMP pip1;RMP pip1;SIG 5;HLW o2;RET 0;";
                //String[] prog3 = {"HLW procesDziecka2;RMP pip1;RMP pip1;RMP pip1;RMP pip1;RMP pip1;RMP pip1;HLW dziecko2Out","RET 0"};
                try {
                    int parentid = Kernel.processManager.createProcess(Kernel.initProc, "proces1", prog1);
                    //Thread.sleep(100);
                    Kernel.processManager.createProcess(Kernel.processManager.getProcessByID(parentid), "proces2", prog2);
                    Thread.sleep(1);
                    //Kernel.processManager.createProcess(Kernel.processManager.getProcessByID(parentid), "proces3", prog3);
                } catch (InterruptedException e) {
                    Kernel.processManager.err(e.getMessage());
                }
                break;
            }
            case "testPipesSync1": {
                String prog1 = "HLW p1;COP pip1 0;SMP pip1 F;SMP pip1 E;SMP pip1 D;SMP pip1 C;SMP pip1 B;SMP pip1 A;SMP pip1 Z;SMP pip1 Y;SMP pip1 X;DOP pip1;HLW o1;RET 0;";
                //String[] prog3 = {"HLW procesDziecka2;RMP pip1;RMP pip1;RMP pip1;RMP pip1;RMP pip1;RMP pip1;HLW dziecko2Out","RET 0"};
//                    int parentid = Kernel.processManager.createProcess(Kernel.initProc, "proces1", prog1);
//                    //Thread.sleep(100);
                    Kernel.processManager.createProcess(Kernel.initProc, "proces1", prog1);
                    //Kernel.processManager.createProcess(Kernel.processManager.getProcessByID(parentid), "proces3", prog3);

                break;
            }
            case "testPipesSync2": {
                String prog2 = "HLW p2;RMP pip1;POP A;PRT A;RMP pip1;POP A;PRT A;RMP pip1;POP A;PRT A;RMP pip1;POP A;PRT A;RMP pip1;POP A;PRT A;RMP pip1;POP A;PRT A;RMP pip1;POP A;PRT A;RMP pip1;POP A;PRT A;RMP pip1;POP A;PRT A;HLW o2;RET 0;";
                //String[] prog3 = {"HLW procesDziecka2;RMP pip1;RMP pip1;RMP pip1;RMP pip1;RMP pip1;RMP pip1;HLW dziecko2Out","RET 0"};

                    Kernel.processManager.createProcess(Kernel.processManager.getProcessByID(Integer.parseInt(commands[1])), "proces2", prog2);
                    //Kernel.processManager.createProcess(Kernel.processManager.getProcessByID(parentid), "proces3", prog3);

                break;
            }
            case "load": {
                try {
                    if (commands.length == 2)
                        Kernel.processManager.createProcess(Kernel.initProc, commands[1], Kernel.disk.getFile(commands[1]));
                    else if (commands.length == 3) {
                        Kernel.processManager.createProcess(Kernel.processManager.getProcessByID(Integer.parseInt(commands[2])), commands[1], Kernel.disk.getFile(commands[1]));
                    }
                } catch (java.io.FileNotFoundException ex) {
                    stdout("Nie ma takiego pliku.");
                }
                break;
            }
            case "touch": {
                if (commands.length == 2) {
                    try {
                        INode file = Kernel.disk.createFile(commands[1], 511);
                        Kernel.disk.resizeFile(file, 128);
                    } catch (Throwable e) {
                        Kernel.processManager.err(e.getMessage());
                    }
                } else {
                    Kernel.shell.stdout("Błędna liczba parametrów:\ntouch [nazwa]");
                }
                break;
            }
            case "rm": {
                if (commands.length == 2) {
                    Kernel.disk.deleteFile(commands[1]);
                } else {
                    Kernel.shell.stdout("Błędna liczba parametrów:\nrm [nazwa]");
                }
                break;
            }
            case "loadFile": {
                if (commands.length == 3) {
                    try {
                        Kernel.disk.loadFile(commands[1], commands[2], 511);
                    } catch (IOException e) {
                        Kernel.shell.stdout("Nie udało się wczytać pliku: " + commands[2] + "\n" + e.getMessage());
                    }
                } else
                    Kernel.shell.stdout("Niepoprawna liczba argumentów:\nload [filename] [real path]");
                break;
            }
            case "ls": {
                if (commands.length == 2 && commands[1].equals("-l")) {
                    Kernel.disk.printFilesList();
                } else if (commands.length == 1) {
                    Kernel.disk.printFiles();
                    stdout("");
                } else {
                    Kernel.shell.stdout("Nieprawidłowy argument:\nls {-l}");
                }
                break;
            }
            case "listPipes": {
                Kernel.pipes.listAllPipes();
                break;
            }
            case "printDisk": {
                Kernel.disk.printDisk();
                break;
            }
            case "cat": {
                if (commands.length == 2) {

                    try {
                        INode file = Kernel.disk.getFile(commands[1]);
                        char[] filedata = Kernel.disk.readFile(file, 0, file.size, Kernel.initProc);
                        for (char chaR : filedata) {
                            Kernel.shell.stdout(""+chaR,true);
                        }
                        stdout("");
                    } catch (java.io.FileNotFoundException ex) {
                        stdout("Nie ma takiego pliku.");
                    }
                }
                break;
            }
            case "ramStatus":{
                if(commands.length == 1) {
                    Kernel.memory.ramStatus();
                }
                else if(commands.length == 2){
                    Kernel.processManager.processRamStatus(Integer.parseInt(commands[1]));
                }
                break;
            }
            case ("printFlags"): {
                if (commands.length == 2) {
                    int processID = Integer.parseInt(commands[1]);
                    try {
                        ProcessNode process = Kernel.processManager.getProcessByID(processID);
                        for (int i = 0; i < Kernel.asm.registerNames.length; i++) {
                            Kernel.shell.stdout("Register " + Kernel.asm.registerNames[i] + ": " + (int) process.getRegisters().AtoD[i]);
                        }
                    } catch (Throwable e) {
                        Kernel.shell.stdout("Wystąpił błąd: " + "\n" + e.getMessage());
                        e.printStackTrace();
                    }
                } else
                    Kernel.shell.stdout("Niepoprawna liczba argumentów:\nprintFlags [processID]");
                break;
            }
            case "help": {
                Kernel.shell.stdout("Lista komend systemu BinBux® v2.1.3.7");
                Kernel.shell.stdout("cat [filename] - wyświetla zawartość pliku");
                Kernel.shell.stdout("help - wyświetla ten komunikat");
                Kernel.shell.stdout("kill (-a) [pid] - zabija proces o podanym numerze. W przypadku użycia parametru '-a' zabija wszystkie procesy.");
                Kernel.shell.stdout("load [filename] ([pid]) - uruchamia program zapisany na dysku. Można podać numer procesu rodzica");
                Kernel.shell.stdout("loadFile [filename] [path] - tworzy na dysku kopię pliku z zewnątrz");
                Kernel.shell.stdout("ls (-l) - wyświetla listę plików na dysku. Użycie parametru -l wyświetla szczegółową listę");
                Kernel.shell.stdout("printDisk - wyświetla surową zawartość dysku");
                Kernel.shell.stdout("ps - wyświetla listę aktualnie pracujących procesów");
                Kernel.shell.stdout("ramStatus ([pid]) - wyświetla informacje o zajętości ramek w pamięci. W przypadku podania pid w argumencie wyświetla tablicę stronic podanego procesu");
                Kernel.shell.stdout("rm [filename] - usuwa podany plik");
                Kernel.shell.stdout("run ([nazwa]) ([id_rodzica]) ([kod]) - uruchamia proces z podanymi parametrami");
                Kernel.shell.stdout("testCond - tworzy testowe procesy, które mają sprawdzać działanie zmiennych warunkowych");
                Kernel.shell.stdout("testLock - tworzy testowe procesy, które mają sprawdzać działanie zamków");
                Kernel.shell.stdout("testPipes - tworzy testowe procesy, które mają sprawdzać działanie potoków");
                Kernel.shell.stdout("listPipes - wyświetla istniejące potoki w systemie");
                Kernel.shell.stdout("printFlags [pid] - wyświetla zawartości flag dla danego procesu");
                Kernel.shell.stdout("touch [filename] - tworzy pusty testowy plik o rozmiarze 128 B");
                Kernel.shell.stdout("tree - wyświetla aktualne procesy w postaci drzewa");
                break;
            }
            default:
                Kernel.shell.stdout("Nieprawidłowe polecenie");

        }
    }
    private static void recursivePrint(ProcessNode root, int level) {
        displayProcessDetails(root, level);
        for (ProcessNode child : root.getChildren()) {
            recursivePrint(child, level + 1);
        }
    }

    private static void displayProcessDetails(ProcessNode process, int level) {
        for (int i = 0; i < level; i++)
            Kernel.shell.stdout("\t",true);
        if (process.getPID() == 1)
            Kernel.shell.stdout(process.getPID() + " " + process.getName() + ", state: " + process.getState().toString());
        else
            Kernel.shell.stdout(process.getPID() + " " + process.getName() + ", state: " + process.getState().toString());

    }
}
