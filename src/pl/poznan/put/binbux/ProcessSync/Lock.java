package pl.poznan.put.binbux.ProcessSync;
import pl.poznan.put.binbux.Kernel;
import pl.poznan.put.binbux.ProcessModule.ProcessNode;
import pl.poznan.put.binbux.ProcessModule.ProcessState;
import java.util.ArrayList;


class Lock {
    private int number;
    private boolean lockStatus = false;
    private ProcessNode block = null;
    private ArrayList <ProcessNode> ProcessTable = new ArrayList<>();

    Lock (int number){
        this.number = number;
    }


    void unlock(ProcessNode process){
        if(block == process){

            lockStatus = false;
            block = null;

            Kernel.shell.log("synchronizacji","Zamek o numerze " + number + " został odblokowany przez proces o numerze " + process.getPID() + ".");

            if(!ProcessTable.isEmpty()){
                block =  ProcessTable.get(0);
                ProcessTable.remove(block);
                lockStatus = true;
                block.setState(ProcessState.READY);
                Kernel.shell.log("synchronizacji","Zamek o numerze " + number + " został zablokowany przez proces o numerze " + process.getPID() + ".");

            }
        }
    }


    public void lock(ProcessNode process){
        if(ProcessTable.isEmpty() && block == null) {
            block = process;
            lockStatus = true;
            Kernel.shell.log("synchronizacji","Zamek o numerze " + number + " został zablokowany przez proces o numerze " + process.getPID() + ".");
        }
        else{
            ProcessTable.add(process);
            process.setState(ProcessState.WAIT);
            Kernel.shell.log("synchronizacji", "Proces o numerze " + process.getPID() + " został dodany do kolejki procesów oczekujących.");
        }
    }

    public boolean isLocked(){
        return lockStatus;
    }

}
