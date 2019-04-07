package pl.poznan.put.binbux.Processing;

import pl.poznan.put.binbux.Assembler.AsmInterpreter;
import pl.poznan.put.binbux.Kernel;
import pl.poznan.put.binbux.ProcessModule.ProcessManager;
import pl.poznan.put.binbux.ProcessModule.ProcessNode;

import java.util.ArrayList;

public class Processor {
    int quantum = 5;
    private Core core;
    private ArrayList<ProcessNode> processList;

    AsmInterpreter mAsmInterpreter = Kernel.asm;
    private ProcessManager mProcessManager = Kernel.processManager;

    public boolean booted = true;

    public Processor() {
        processList = new ArrayList<>();
        core = new Core(this);

    }

    public void stop() {
        booted = false;
    }

    public void addProcess(ProcessNode process) {
        processList.add(process);
        Kernel.shell.log("Round Robin", "Dodano proces o PID: "+process.getPID());
    }

    ArrayList<ProcessNode> getList() {
        return processList;
    }

    public void returnProcess(ProcessNode process) {
        processList.remove(process);
        mProcessManager.returnProcess(process);
        Kernel.shell.log("Round Robin", "Zakonczono proces o PID: "+process.getPID());
    }

    public Core getCore() {
        return core;
    }
}
