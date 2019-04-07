package pl.poznan.put.binbux.ProcessModule;

import java.util.Stack;

public class Registers {
    public char[] AtoD = {0, 0, 0, 0};
    public char PC = 0;
    public char RecentPC=0;
    public boolean ZERO = false;
    public Stack<Character> stack = new Stack<>();
}
