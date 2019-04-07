package pl.poznan.put.binbux.Assembler;

import pl.poznan.put.binbux.HardDisk.INode;
import pl.poznan.put.binbux.Kernel;
import pl.poznan.put.binbux.ProcessModule.ProcessNode;

import java.security.InvalidParameterException;

public class AsmInterpreter {
    public char[] registerNames = {'A', 'B', 'C', 'D'};

    private boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private int getRegisterIndex(String first) {
        int indexOfRegister1 = 0;
        for (int i = 0; i < registerNames.length; i++) {
            if (registerNames[i] == first.charAt(0)) {
                indexOfRegister1 = i;
                break;
            }
        }
        return indexOfRegister1;
    }

    private void setRegister(ProcessNode process, String first, String sec) {
        if (isNumeric(sec)) {
            char value = (char) Integer.parseInt(sec);
            int indexOfRegister1 = getRegisterIndex(first);
            process.getRegisters().AtoD[indexOfRegister1] = value;
        } else {
            int indexOfRegister1 = getRegisterIndex(first);
            int indexOfRegister2 = getRegisterIndex(sec);
            process.getRegisters().AtoD[indexOfRegister1] = process.getRegisters().AtoD[indexOfRegister2];
        }
    }

    private char getRegisterValue(ProcessNode process, String first) {
        int indexOfRegister1 = getRegisterIndex(first);
        return process.getRegisters().AtoD[indexOfRegister1];
    }

    public void ProcessInstruction(ProcessNode process) {
        String instruction = Kernel.memory.readInstruction(process.getRegisters().PC, process);
        process.getRegisters().RecentPC = process.getRegisters().PC;
        process.getRegisters().PC += instruction.length() + 1;
        try {
            Kernel.shell.log("asemblera", "Wykonanie instrukcji " + instruction + " przez proces " + process.getName());
            Execute(instruction, process);
        } catch (Throwable e) {
            Kernel.cpu.returnProcess(process);
            Kernel.processManager.err("Killed process PID: " + process.getPID() + ", name: " + process.getName() + ", because an error occurred: " + e.toString()  + " at instruction: " + instruction);
            e.printStackTrace();
        }
    }

    private void Execute(String instruction, ProcessNode process) throws Throwable {
        /*if(process.getPID() != 1)
            Kernel.shell.stdout("Processing PID: "+process.getPID()+", name: "+process.getName()+": "+instruction);*/
        instruction = instruction.replace(",", "");
        String[] instrArr = instruction.split(" ");
        switch (instrArr[0].toUpperCase()) {
            case ("MOV"): {
                //implementacja tego co ma robić MOV
                setRegister(process, instrArr[1], instrArr[2]);
                break;
            }
            case ("PSH"): {
                if (isNumeric(instrArr[1])) {
                    process.getRegisters().stack.push((char) Integer.parseInt(instrArr[1]));
                } else {
                    process.getRegisters().stack.push(getRegisterValue(process, instrArr[1]));
                }
                break;
            }
            case ("POP"): {
                int indexOfRegister1 = getRegisterIndex(instrArr[1]);
                process.getRegisters().AtoD[indexOfRegister1] = process.getRegisters().stack.pop();
                break;
            }
            case ("CP"): {
                Kernel.processManager.createProcess(process, instrArr[1], Kernel.disk.getFile(instrArr[1]));
                break;
            }
            case ("HLW"): {
                Kernel.shell.stdout(instrArr[1]);
                break;
            }
            case ("DEC"): {
                int indexOfRegister1 = getRegisterIndex(instrArr[1]);
                process.getRegisters().AtoD[indexOfRegister1]--;
                process.getRegisters().ZERO = ((int) process.getRegisters().AtoD[indexOfRegister1] == 0);
                break;
            }
            case ("INC"): {
                int indexOfRegister1 = getRegisterIndex(instrArr[1]);
                process.getRegisters().AtoD[indexOfRegister1]++;
                break;
            }
            case ("PRINTFLAGS"): {
                for (int i = 0; i < registerNames.length; i++) {
                    Kernel.shell.stdout("Register " + registerNames[i] + ": " + (int) process.getRegisters().AtoD[i]);
                }
                break;
            }
            case ("SLP"): {
                int sleepCount = Integer.parseInt(instrArr[1]);
                Thread.sleep(sleepCount);
                break;
            }
            case ("PRT"): {
                Kernel.shell.stdout("" + (int)getRegisterValue(process, instrArr[1]));
                break;
            }
            case ("CMP"): {
                if (isNumeric(instrArr[2])) {
                    char value = (char) Integer.parseInt(instrArr[2]);
                    int indexOfRegister1 = 0;
                    for (int i = 0; i < registerNames.length; i++) {
                        if (registerNames[i] == instrArr[1].charAt(0)) {
                            indexOfRegister1 = i;
                            break;
                        }
                    }
                    process.getRegisters().ZERO = process.getRegisters().AtoD[indexOfRegister1] == value;
                } else {
                    int indexOfRegister1 = getRegisterIndex(instrArr[1]);
                    int indexOfRegister2 = getRegisterIndex(instrArr[2]);
                    process.getRegisters().ZERO = process.getRegisters().AtoD[indexOfRegister1] == process.getRegisters().AtoD[indexOfRegister2];
                }
                break;

            }
            case ("JNZ"): {
                if (!process.getRegisters().ZERO) {
                    int offset = Integer.parseInt(instrArr[1]);
                    process.getRegisters().PC = (char) offset;
                }
                break;
            }
            case ("JZ"): {
                if (process.getRegisters().ZERO) {
                    int offset = Integer.parseInt(instrArr[1]);
                    process.getRegisters().PC = (char) offset;
                }
                break;
            }
            case ("JMP"): {
                int offset = Integer.parseInt(instrArr[1]);
                process.getRegisters().PC = (char) offset;
                break;
            }
            case ("ADD"): {
                int indexOfRegister1 = getRegisterIndex(instrArr[1]);
                char value;
                if (isNumeric(instrArr[2])) {
                    value = (char) Integer.parseInt(instrArr[2]);
                } else {
                    value = getRegisterValue(process, instrArr[2]);
                }
                process.getRegisters().AtoD[indexOfRegister1] += value;
                break;
            }
            case ("SUB"): {
                int indexOfRegister1 = getRegisterIndex(instrArr[1]);
                char value;
                if (isNumeric(instrArr[2])) {
                    value = (char) Integer.parseInt(instrArr[2]);
                } else {
                    value = getRegisterValue(process, instrArr[2]);
                }
                process.getRegisters().AtoD[indexOfRegister1] -= value;
                break;
            }
            case ("MUL"): {
                int indexOfRegister1 = getRegisterIndex(instrArr[1]);
                char value;
                if (isNumeric(instrArr[2])) {
                    value = (char) Integer.parseInt(instrArr[2]);
                } else {
                    value = getRegisterValue(process, instrArr[2]);
                }
                process.getRegisters().AtoD[indexOfRegister1] *= value;
                break;
            }

            //Lock - zablokowanie zamka
            case ("LCK"): {
                if (instrArr.length != 2) {
                    throw new IllegalArgumentException("LCK takes 1 argument");
                }
                Kernel.syncer.lock(Integer.parseInt(instrArr[1]), process);
                break;
            }

            //Unlock - odblokowanie zamka
            case ("UNL"): {
                if (instrArr.length != 2) {
                    throw new IllegalArgumentException("UNL takes 1 argument");
                }
                Kernel.syncer.unlock(Integer.parseInt(instrArr[1]), process);
                break;
            }

            //Wait - wait na zmiennej warunkowej
            case ("WAT"): {
                if (instrArr.length != 2) {
                    throw new IllegalArgumentException("WAT takes 1 argument");
                }
                Kernel.syncer.wait(Integer.parseInt(instrArr[1]), process);
                break;
            }

            //Signal - sygnał na zmiennej warunkowej
            case ("SIG"): {
                if (instrArr.length != 2) {
                    throw new IllegalArgumentException("LSG takes 1 argument");
                }
                Kernel.syncer.signal(Integer.parseInt(instrArr[1]));
                break;
            }

            //Signal broadcast - broadcast na zmiennej warunkowej
            case ("SIB"): {
                if (instrArr.length != 2) {
                    throw new IllegalArgumentException("LSB takes 1 argument");
                }
                Kernel.syncer.broadcast(Integer.parseInt(instrArr[1]));
                break;
            }

            //Create Ordinary Pipe = tworzy potok nienazwany
            case ("COP"): {
                if (instrArr.length != 3) {
                    throw new IllegalArgumentException("LSB takes 2 argument");
                }
                boolean argTwo = (Integer.parseInt(instrArr[2]) != 0);
                Kernel.pipes.createPipe(process, instrArr[1], argTwo);
                break;
            }
            //Delete Ordinary Pipe = usuwa potok nienazwany
            case ("DOP"): {
                if (instrArr.length != 2) {
                    throw new IllegalArgumentException("LSB takes 1 argument");
                }
                Kernel.pipes.deletePipe(process, instrArr[1]);
                break;
            }
            //Send Message to Pipe = wysyła bajt do potoku
            case ("SMP"): {
                if (instrArr.length != 3) {
                    throw new IllegalArgumentException("LSB takes 2 argument");
                }
                Kernel.pipes.sendMessage(process, instrArr[1], instrArr[2].charAt(0));
                break;
            }
            //Read Message from Pipe = odbiera bajt z potoku
            case ("RMP"): {
                if (instrArr.length != 2) {
                    throw new IllegalArgumentException("LSB takes 1 argument");
                }
                Character receivedByte = Kernel.pipes.readMessage(process, instrArr[1]);
                process.getRegisters().stack.push(receivedByte);
                break;
            }

            //Return - zakończenie wykonywania programu
            case ("RET"): {
                if (instrArr.length != 2) {
                    throw new IllegalArgumentException("RET takes 1 argument");
                }
                Kernel.processManager.err(process, Integer.parseInt(instrArr[1]));
                Kernel.cpu.returnProcess(process);
                break;
            }
            //WRF testfile register
            case ("WRF"): {
                if (instrArr.length != 3) {
                    throw new IllegalArgumentException("WRF takes 2 argument");
                }
                int value = getRegisterValue(process, instrArr[2]);
                char[] buffer = new char[1];
                buffer[0] = (char)value;
                Kernel.disk.writeFile(Kernel.disk.getFile(instrArr[1]),buffer,1,0,process);
                break;
            }
            case ("RDF"): {
                if (instrArr.length != 2) {
                    throw new IllegalArgumentException("RDF takes 1 argument");
                }
                INode file = Kernel.disk.getFile(instrArr[1]);
                char[] buffer = Kernel.disk.readFile(file,0,file.size,process);
                for(int i=0;i<buffer.length;i++) {
                    if(buffer[i]>='0' && buffer[i] <='9') {
                        int liczba = buffer[i] - '0';
                        process.getRegisters().stack.push((char) liczba);
                    }
                }
                break;
            }
            case ("CRF"): {
                if (instrArr.length != 2) {
                    throw new IllegalArgumentException("CRF takes 1 argument");
                }
                Kernel.disk.createFile(instrArr[1],511);
                break;
            }
            case ("RMF"): {
                if (instrArr.length != 2) {
                    throw new IllegalArgumentException("RMF takes 1 argument");
                }
                Kernel.disk.deleteFile(instrArr[1]);
                break;
            }

            default:
                throw new InvalidParameterException("Wykonanie nieznanej instrukcji: " + instrArr[0]);
        }

    }
}
