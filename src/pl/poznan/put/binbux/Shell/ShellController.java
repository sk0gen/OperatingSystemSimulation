package pl.poznan.put.binbux.Shell;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pl.poznan.put.binbux.Kernel;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;

public class ShellController implements javafx.fxml.Initializable {
    @FXML public TextArea stdout;
    @FXML public TextArea logout;
    @FXML public TextField cmdinput;
    @FXML public Button stepbut;
    @FXML public CheckBox stepcheck;
    private Thread ShellThread;
    public Vector<String> cmdHistory = new Vector<String>();
    int curCmdIndex = 0;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        stdout.setEditable(false);
        stdout.setFocusTraversable(false);
        logout.setFocusTraversable(false);
        logout.setEditable(false);
        stdout.setFont(Font.font("Courier New", FontWeight.NORMAL, 12));
        logout.setFont(Font.font("Courier New", FontWeight.NORMAL, 12));
        cmdinput.setFont(Font.font("Courier New", FontWeight.NORMAL, 12));
        cmdinput.requestFocus();
        Shell shell = new Shell(this);
        cmdinput.setOnAction(e -> {
            String cmd = cmdinput.getText();
            cmdinput.setText("");
            shell.stdout("BinBux 2.137 #> "+cmd);
            try {
                cmdHistory.add(cmd);
                shell.ParseCommand(cmd);
            } catch(Throwable ex) {
                Kernel.shell.stdout("Nastąpił wyjątek podczas parsownaia komendy: "+ex.toString());
                ex.printStackTrace();
            }
        });
        stepbut.setDisable(true);
        stepcheck.setOnAction(e -> {
            Kernel.shell.autoStep = !stepcheck.isSelected();
            if(!Kernel.shell.autoStep) {
                stepbut.setDisable(false);
            } else {
                stepbut.setDisable(true);
            }
        });
        stepbut.setOnAction(e ->{
            ShellRunner.step = true;
        });
        cmdinput.setOnKeyReleased(e ->{
            if(e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                cmdinput.positionCaret(cmdinput.getText().length());
            }
            if(cmdHistory.size() == 0)
                return;
            if(e.getCode() == KeyCode.UP) {
                String cmd = cmdinput.getText();
                if(cmd.equals(cmdHistory.get(curCmdIndex))) {
                    if(curCmdIndex > 0) {
                        curCmdIndex--;
                        cmdinput.setText(cmdHistory.get(curCmdIndex));
                    }
                } else {
                    curCmdIndex = cmdHistory.size()-1;
                    cmdinput.setText(cmdHistory.get(curCmdIndex));
                }
            }
            if(e.getCode() == KeyCode.DOWN) {
                String cmd = cmdinput.getText();
                if(cmd.equals(cmdHistory.get(curCmdIndex))) {
                    if(curCmdIndex < cmdHistory.size()-1) {
                        curCmdIndex++;
                        cmdinput.setText(cmdHistory.get(curCmdIndex));
                    }
                } else {
                    curCmdIndex = cmdHistory.size()-1;
                    cmdinput.setText(cmdHistory.get(curCmdIndex));
                }
            }
            if(e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                cmdinput.positionCaret(cmdinput.getText().length());
            }
        });
        ShellThread = new Thread(new ShellRunner(shell));
        ShellThread.start();
    }

}
