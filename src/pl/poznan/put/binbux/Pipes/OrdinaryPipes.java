package pl.poznan.put.binbux.Pipes;

import pl.poznan.put.binbux.Kernel;
import pl.poznan.put.binbux.ProcessModule.ProcessNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static pl.poznan.put.binbux.Kernel.shell;
import static pl.poznan.put.binbux.Kernel.syncer;

public class OrdinaryPipes {
    private int CVnumer = 8;
    private Map<String, OrdinaryPipe> pipesMap = new HashMap<>();
    private String moduleName = "potoków";

    public Boolean createPipe(ProcessNode process, String pipeID, Boolean direction) {

        if(this.pipesMap.containsKey(pipeID)) {
            shell.log(moduleName, "Błąd: Potok o podanym id nie istnieje.");
            return false;
        }
        OrdinaryPipe ordinaryPipe = new OrdinaryPipe(process, pipeID, direction,CVnumer++);
        this.pipesMap.put(pipeID, ordinaryPipe);
        shell.log(moduleName, "Proces  " + process.getName() + " utworzył potok o id " + pipeID + ".");

        return true;
    }

    public Boolean deletePipe(ProcessNode process, String pipeID) {

        if(!this.pipesMap.containsKey(pipeID)) {
            shell.log(moduleName, "Błąd: Potok o podanym id nie istnieje.");
            return false;
        }

        if(this.pipesMap.get(pipeID).getParentProcess().getPID() == process.getPID()) {
            if(this.pipesMap.get(pipeID).isEmpty()) {
                this.pipesMap.remove(pipeID);
                shell.log(moduleName, "Proces  " + process.getName() + " usunął potok o id " + pipeID + ".");
                return true;
            } else {
                process.getRegisters().PC = process.getRegisters().RecentPC;
                this.pipesMap.get(pipeID).condVariable.wait(process);
                return false;
            }
        }

        shell.log(moduleName, "Błąd: Brak uprawnień, aby usunąć potok o id " + pipeID + ".");
        return false;
    }

    public Boolean deleteAllPipes(ProcessNode process) {
        for (Map.Entry<String, OrdinaryPipe> entry : pipesMap.entrySet()) {
           if (entry.getValue().getParentProcess().getPID() == process.getPID()) {
               this.pipesMap.remove(entry.getKey());
               shell.log(moduleName, "Usunięto potok o id " + entry.getValue().getName() + " należący do procesu " + process.getName() + ".");
               return true;
           }
        }
        return false;
    }

    public Boolean sendMessage(ProcessNode process, String pipeID, char oneByte) {
        if(!this.pipesMap.containsKey(pipeID)) {
            shell.log(moduleName, "Błąd: Potok o podanym id nie istnieje.");
            return false;
        }

        OrdinaryPipe pipe = this.pipesMap.get(pipeID);

        char perm = this.processPermission(process, pipeID);

        if(perm == 'F') {
            shell.log(moduleName, "Błąd: Proces " + process.getName() + " nie posiada uprawnień pisania do potoku o id "+ pipeID +".");
            return false;
        }

        if(((perm == 'P') && !pipe.getDirection()) || ((perm == 'C') && pipe.getDirection())) {
            if(pipe.addData(process, oneByte)) {
                shell.log(moduleName, "Proces " + process.getName() + " wysłał do potoku o id " + pipeID + " bajt danych: " + oneByte + ".");
                return true;
            }

        }

        shell.log(moduleName, "Błąd: Zapis do potoku o id " + pipeID +" przez proces " + process.getName() +" nie powiódł się.");
        return false;
    }

    public Character readMessage(ProcessNode process, String pipeID) {
        if(!this.pipesMap.containsKey(pipeID)) {
            shell.log(moduleName, "Błąd: Potok o podanym id nie istnieje.");
            return null;
        }

        OrdinaryPipe pipe = this.pipesMap.get(pipeID);

        char perm = this.processPermission(process, pipeID);

        if(perm == 'F') {
            shell.log(moduleName, "Błąd: Proces " + process.getName() + " nie posiada uprawnień odczytu potoku o id "+ pipeID +".");
            return null;
        }

        if(((perm == 'P') && pipe.getDirection()) || ((perm == 'C') && !pipe.getDirection())) {
            Character oneByte = pipe.getData(process);
            if(oneByte != null)
                shell.log(moduleName, "Proces " + process.getName() + " odczytał z potoku o id " + pipeID + " bajt danych: " + oneByte + ".");
            return oneByte;
        }

        shell.log(moduleName, "Błąd: Zapis do potoku o id " + pipeID +" przez proces " + process.getName() +" nie powiódł się.");
        return null;
    }

    private char processPermission(ProcessNode process, String pipeID) {
        if(!this.pipesMap.containsKey(pipeID)) {
            return 'F'; // FALSE
        }

        if(this.pipesMap.get(pipeID).getParentProcess().getPID() == process.getPID()){
            return 'P'; // PARENT
        }

        Vector<ProcessNode> children = this.pipesMap.get(pipeID).getParentProcess().getChildren();
        for (ProcessNode child : children) {
            if (child.getPID() == process.getPID()) {
                return 'C'; // CHILD
            }
        }

        return 'F';

    }

    public void listAllPipes() {
        if(this.pipesMap.isEmpty()) {
            shell.stdout("Brak istniejących potoków.");
        } else {
            shell.stdout( "Lista istniejących potoków:");
            for (Map.Entry<String, OrdinaryPipe> entry : pipesMap.entrySet()) {
                shell.stdout( "- "+entry.getValue().getName()+" (właściciel: "+ entry.getValue().getParentProcess().getName()+", zajęte "+entry.getValue().queueSize()+" bajtów)");
            }
        }

    }

}
