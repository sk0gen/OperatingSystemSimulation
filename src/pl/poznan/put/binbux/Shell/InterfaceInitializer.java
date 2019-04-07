package pl.poznan.put.binbux.Shell;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;
import pl.poznan.put.binbux.Kernel;

import java.net.URL;
import java.util.ResourceBundle;

public class InterfaceInitializer extends Application {
    ShellController controller;
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ShellLayout.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("BinBux v2.1.3.7");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            Kernel.cpu.booted = false;
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
