package pl.poznan.put.binbux.Shell;

import pl.poznan.put.binbux.Kernel;

public class ShellRunner implements Runnable {
    public static boolean step;
    private Shell shell;
    public ShellRunner(Shell _shell) {
        this.shell = _shell;
    }
    @Override
    public void run() {
        Kernel.init(shell);
        step = false;
        while(Kernel.cpu.booted) {
            if(step || Kernel.shell.autoStep){
                Kernel.cpu.getCore().step();
                step = false;
            } else {

            }
            try {
                Thread.sleep(10);
            }catch (InterruptedException e){
                System.err.println(e.getMessage());
            }
        }
        Kernel.disk.SaveVHD();
    }
}
