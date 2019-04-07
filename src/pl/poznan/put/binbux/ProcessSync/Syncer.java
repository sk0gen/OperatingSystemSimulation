package pl.poznan.put.binbux.ProcessSync;

import pl.poznan.put.binbux.Kernel;
import pl.poznan.put.binbux.ProcessModule.ProcessNode;

import java.util.Hashtable;


public class Syncer {
    private Hashtable<Integer, Lock> locks = new Hashtable<>();
    private Hashtable<Integer, CondVar> condvars = new Hashtable<>();

    public Syncer() {
        Kernel.syncer = this;
        for (int i = 0; i < 8; i++) {
            locks.put(i, new Lock(i));
        }
        for (int i = 0; i < 8; i++) {
            condvars.put(i, new CondVar(i));
        }
        Kernel.shell.log("synchronizacji","Utworzono moduł synchronizacji.");
    }

    private Lock getLock(int number) {
        return locks.get(number);
    }

    public void lock(int numer, ProcessNode process) { getLock(numer).lock(process);
    }

    public boolean tryLock(int numer,ProcessNode process){
        if(getLock(numer).isLocked())
            return false;
        else{
            lock(numer,process);
            return true;
        }
    }

    public void unlock(int numer, ProcessNode process) {
        getLock(numer).unlock(process);
    }

    private CondVar getCondVar(int number) {
        return condvars.get(number);
    }

    public void signal(int number) {
        getCondVar(number).signal();
    }

    public void broadcast(int number){
        getCondVar(number).broadcast();
    }

    public void wait(int number, ProcessNode process){
        getCondVar(number).wait(process);
    }
}
