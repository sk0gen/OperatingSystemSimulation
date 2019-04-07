package pl.poznan.put.binbux.Pipes;

import pl.poznan.put.binbux.ProcessSync.CondVar;
import pl.poznan.put.binbux.ProcessModule.ProcessNode;

import java.util.LinkedList;

class OrdinaryPipe {
    private Integer MAX_BUFOR_SIZE = 8;
    private LinkedList<Character> queue = new LinkedList<>();
    private ProcessNode parentProcess;
    private Boolean direction;
    private String pipeID;
    public CondVar condVariable;

    OrdinaryPipe(ProcessNode parentProcess, String pipeID, Boolean direction, int numer) {
        this.parentProcess = parentProcess;
        this.pipeID = pipeID;
        this.direction = direction;
        this.condVariable = new CondVar(numer);
    }

    Boolean addData(ProcessNode process, Character oneByte) {
        if (this.queue.size() < MAX_BUFOR_SIZE) {
            this.queue.addFirst(oneByte);
            condVariable.broadcast();
            return true;
        } else {
            process.getRegisters().PC = process.getRegisters().RecentPC;
            condVariable.wait(process);
            return false;
        }
    }

    Character getData(ProcessNode process) {
        if (this.queue.size() > 0) {
            Character lastByte = this.queue.getLast();
            this.queue.removeLast();
            condVariable.broadcast();
            return lastByte;
        } else {
            process.getRegisters().PC = process.getRegisters().RecentPC;
            condVariable.wait(process);
            return null;
        }
    }

    ProcessNode getParentProcess() {
        return parentProcess;
    }

    Boolean getDirection() {
        return direction;
    }

    String getName() {
        return pipeID;
    }

    Boolean isEmpty() {
        return this.queue.size() == 0;
    }

    Integer queueSize() {
        return this.queue.size();
    }

}
