package pl.poznan.put.binbux.ProcessModule;

import pl.poznan.put.binbux.HardDisk.INode;
import pl.poznan.put.binbux.Kernel;

import java.util.*;

public class ProcessManager {
    private ProcessNode init;
    private int pidCounter = 1;

    public ProcessManager() {
        Kernel.shell.log("zarządzania procesami", "Utworzono menedżer procesów");
        init = new ProcessNode(getNewPID(), null, "INIT", null);
    }

    private void scanTreeToList(ProcessNode root, List<ProcessNode> list) {
        if (root.getPID() != 1) list.add(root);
        if (!root.getChildren().isEmpty()) {
            for (ProcessNode proc : root.getChildren()) {
                scanTreeToList(proc, list);
            }
        }
    }

    private int getNewPID() {
        int pid = pidCounter++;
        Kernel.shell.log("zarządzania procesami", "Wygenerowano nowy pid: " + pid);
        return pid;
    }


    public int createProcess(ProcessNode parent, String name, String program) {
        int pid = getNewPID();
        ProcessNode newnode = new ProcessNode(pid, parent, name, null);
        newnode.setPagetable(Kernel.memory.load(program, newnode));
        parent.addChild(newnode);
        Kernel.shell.log("zarządzania procesami", "Wprowadzono właściwości procesu:\n\tnazwa: " + newnode.getName() + "\n\tpid: " + pid + "\n\tdługość programu: " + newnode.getLength() + " bajty");
        Kernel.cpu.addProcess(newnode);
        newnode.setState(ProcessState.READY);
        return pid;
    }

    public int createProcess(ProcessNode parent, String name, INode file) {
        int pid = getNewPID();
        ProcessNode newnode = new ProcessNode(pid, parent, name, null);
        newnode.setPagetable(Kernel.memory.load(file, newnode));
        parent.addChild(newnode);
        Kernel.shell.log("zarządzania procesami", "Wprowadzono właściwości procesu:\n\tnazwa: " + newnode.getName() + "\n\tpid: " + pid + "\n\tdługość programu: " + newnode.getLength() + " bajty");
        Kernel.cpu.addProcess(newnode);
        newnode.setState(ProcessState.READY);
        return pid;
    }

    public List<ProcessNode> getAllProcesses() {
        List<ProcessNode> allProcesses = new ArrayList<>();
        ProcessNode root = init;
        scanTreeToList(root, allProcesses);
        return allProcesses;
    }

    public void returnProcess(ProcessNode process) {
        Kernel.shell.log("zarządzania procesami", "Proces \"" + process.getName() + "\" (" + process.getPID() + ") został oddany do usunięcia");
        Kernel.memory.free(process);
        process.kill();
    }


    public ProcessNode getProcessByID(int pid) {
        if (pid == 1) return init;
        for (ProcessNode proc : getAllProcesses()) {
            if (proc.getPID() == pid) return proc;
        }
        return null;
    }

    public void err(String message) {
        Kernel.shell.stdout(message);
    }

    public void err(ProcessNode process, int errcode) {
        if (errcode == 0)
            Kernel.shell.stdout("Proces \"" + process.getName() + "\" (" + process.getPID() + ") wykonany poprawnie");
        else
            Kernel.shell.stdout("Proces \"" + process.getName() + "\" (" + process.getPID() + ")zakończony z kodem błędu " + errcode);
    }

    public void processRamStatus(int pid) {
        ProcessNode proces = getProcessByID(pid);
        Kernel.shell.stdout("Stronica\tramka");
        for (int i = 0; i < proces.getPageTable().size(); i++) {
            Kernel.shell.stdout(i + "\t\t\t" + proces.getPageTable().get(i));
        }
    }
}
