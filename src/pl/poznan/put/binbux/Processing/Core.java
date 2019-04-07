package pl.poznan.put.binbux.Processing;

import pl.poznan.put.binbux.Kernel;
import pl.poznan.put.binbux.ProcessModule.*;


public class Core {
    private Processor cpu;
    private int stepCounter = 0;
    private ProcessNode currentProcess;

    Core(Processor parent) {
        cpu = parent;
        currentProcess = Kernel.initProc;
    }

    public void step() {

        if (currentProcess.equals(Kernel.initProc)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
            SwitchContext();
        }

        if (stepCounter >= cpu.quantum) {
            SwitchContext();
            stepCounter = 0;

        }

        stepCounter++;
        //Kernel.shell.log("Round Robin", "Wykonano krok nr: "+stepCounter);

        if (currentProcess.getLength() > currentProcess.getRegisters().PC && currentProcess.getState() == ProcessState.RUNNING) {

            cpu.mAsmInterpreter.ProcessInstruction(currentProcess);

        }
        if (currentProcess.getLength() == 0  || currentProcess.getState() != ProcessState.RUNNING) {
            SwitchContext();
        }

    }

    private void SwitchContext() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e){
            System.err.println(e.getMessage());
        }
        if (currentProcess.getState() == ProcessState.RUNNING)
            currentProcess.setState(ProcessState.READY);
        ProcessNode potentialNext = Kernel.initProc;

        if(cpu.getList().size()>0) {
            ProcessNode swap = cpu.getList().get(0);
            cpu.getList().remove(swap);
                if(swap.getState() != ProcessState.WAIT)
            Kernel.shell.log("Round Robin", "Przerwano proces o PID: "+swap.getPID());
            cpu.getList().add(swap);
            potentialNext = cpu.getList().get(0);
            if(potentialNext.getState() != ProcessState.WAIT)
                Kernel.shell.log("Round Robin", "Rozpoczeto proces o PID: "+potentialNext.getPID());
        }

        currentProcess = potentialNext;

        Kernel.runningProcess.add(currentProcess);
        if (currentProcess.getState() == ProcessState.READY)
            currentProcess.setState(ProcessState.RUNNING);
        stepCounter = 0;
    }
}
