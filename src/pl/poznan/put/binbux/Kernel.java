package pl.poznan.put.binbux;

import pl.poznan.put.binbux.HardDisk.DiskController;
import pl.poznan.put.binbux.MainMemory.RamController;
import pl.poznan.put.binbux.Pipes.OrdinaryPipes;
import pl.poznan.put.binbux.ProcessSync.Syncer;
import pl.poznan.put.binbux.Processing.*;
import pl.poznan.put.binbux.ProcessModule.*;
import pl.poznan.put.binbux.Assembler.*;
import pl.poznan.put.binbux.Shell.*;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Kernel {
    public static Processor cpu;
    public static ProcessManager processManager;
    public static ProcessNode initProc;
    public static AsmInterpreter asm;
    public static ArrayList<ProcessNode> runningProcess = new ArrayList<>();
    public static Syncer syncer;
    public static RamController memory;
    public static DiskController disk;
    public static OrdinaryPipes pipes;
    static JTextArea logger;
    public static Shell shell;

    public static void init(Shell preShell) {
        shell = preShell;
        asm = new AsmInterpreter();
        memory = new RamController();
        processManager = new ProcessManager();
        initProc = processManager.getProcessByID(1);
        cpu = new Processor();
        syncer = new Syncer();
        disk = new DiskController();
        pipes = new OrdinaryPipes();
    }

}
