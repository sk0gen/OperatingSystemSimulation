package pl.poznan.put.binbux.ProcessModule;

import pl.poznan.put.binbux.Kernel;

import java.util.Vector;

public class ProcessNode {
    private int pid;
    private ProcessNode parent;
    private String name;
    private Vector<ProcessNode> children;
    private Registers registers = new Registers();
    private Vector<Integer> pagetable;
    private ProcessState state;
    private int length = 0;

    ProcessNode(int pid, ProcessNode parent, String name, Vector<Integer> pagetable) {
        this.state = ProcessState.NEW;
        this.pid = pid;
        this.parent = parent;
        this.name = name;
        children = new Vector<>();
        this.pagetable = pagetable;
    }

    public int getPID() {
        return pid;
    }


    public String getName() {
        return name;
    }


    public ProcessNode getParent() {
        return parent;
    }


    void addChild(ProcessNode child) {
        children.add(child);
    }


    public Vector<ProcessNode> getChildren() {
        return children;
    }


    public void kill() {
        if (pid == 1) {
            Kernel.shell.stdout("Proces o numerze: " + pid + " nie może być zabity");
            return;
        }
        try {
            Kernel.pipes.deleteAllPipes(this);
            state = ProcessState.TERM;
            ProcessNode newParent = parent;
            while (newParent.getPID() != 1) {
                newParent = newParent.getParent();
            }
            for (ProcessNode child : children) {
                newParent.addChild(child);
                child.setParent(newParent);
            }
            children.clear();
            parent.getChildren().remove(this);
        } catch (Throwable e) {
            Kernel.processManager.err(e.getMessage());
        }
        Kernel.shell.log("zarządzania procesami", "Proces \"" + name + "\" (" + pid + ") usunięty");
    }

    private void setParent(ProcessNode parent) {
        this.parent = parent;
    }

    public Registers getRegisters() {
        return registers;
    }

    public Vector<Integer> getPageTable() {
        return pagetable;
    }

    void setPagetable(Vector<Integer> pagetable) {
        this.pagetable = pagetable;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        Kernel.shell.log("zarządzania procesami", "Zmieniono status procesu \"" + name + "\" (" + pid + ") z " + this.state + " na " + state);
        this.state = state;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
