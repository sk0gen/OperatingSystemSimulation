package pl.poznan.put.binbux.HardDisk;

public class INode {
    INode(int directBlock, int attr){
        directBlock1 = directBlock;
        attributes = attr;
    }
    int directBlock1;
    int directBlock2 = -1;
    int indirectBlock = -1;
    public int size;
    public final int attributes;

}
