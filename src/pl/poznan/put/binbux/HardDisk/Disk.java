package pl.poznan.put.binbux.HardDisk;

class Disk {

    int blockSize = 32;
    int size = 2048;

    private char[] disk = new char[size];

    private void erase(){
        for(int i=0;i<size;i++){
            disk[i] = 255;
        }
    }

    Disk(){
        erase();
    }

    char read(int addr){
        return disk[addr];
    }

    void write(int addr, char value){
        disk[addr] = value;
    }


}
