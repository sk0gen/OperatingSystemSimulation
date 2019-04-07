package pl.poznan.put.binbux.MainMemory;

class Ram {
    final int frameSize = 16;
    final int size = 512;
    private char[] memory = new char[size];


    void write(int address,char value){
        if(address>memory.length)
            throw new IndexOutOfBoundsException("Memory index to high");
        memory[address] = value;
    }
    char read(int address){
        if(address>memory.length)
            throw new IndexOutOfBoundsException("Memory index to high");
        return memory[address];
    }
}
