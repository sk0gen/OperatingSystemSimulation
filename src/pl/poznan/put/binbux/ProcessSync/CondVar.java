package pl.poznan.put.binbux.ProcessSync;
import pl.poznan.put.binbux.Kernel;
import pl.poznan.put.binbux.ProcessModule.ProcessNode;
import pl.poznan.put.binbux.ProcessModule.ProcessState;

import java.util.ArrayList;


public class CondVar {
    private int number;
    private Lock lock;
    private ArrayList<ProcessNode> PTable = new ArrayList<>();


    public CondVar(int number){
        this.number = number;
        lock = new Lock(number);
    }

    public void signal(){
        Kernel.shell.log("synchronizacji", "Zmienna warunkowa o numerze " + number + " wznawia pierwszy z oczekujących procesów.");
        ProcessNode process = PTable.get(0);
        lock.lock(process);
        process.setState(ProcessState.READY);
        lock.unlock(process);
        PTable.remove(process);
    }

    public void broadcast(){
        Kernel.shell.log("synchronizacji","Zmienna warunkowa o numerze " + number + " wznawia wszystkie oczekujące procesy.");
        for(int i=0;i<PTable.size();i++){
            lock.lock(PTable.get(i));
            PTable.get(i).setState(ProcessState.READY);
            lock.unlock(PTable.get(i));
            PTable.remove(PTable.get(i));
        }
    }

    public void wait(ProcessNode process){
        Kernel.shell.log("synchronizacji","Zmienna warunkowa o numerze " + number + " wprowadza proces w stan oczekiwania i dodaje go do kolejki procesów oczekujących na danej zmiennej.");
        lock.lock(process);
        PTable.add(process);
        process.setState(ProcessState.WAIT);
        lock.unlock(process);
    }

    int getNumber(){
        return number;
    }

}
