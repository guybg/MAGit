package com.magit.gui.utils;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class BrowseHandler {
    public static void browseFolder(Button source, TextField destinationLocationTextField) {
        Button chosen = source;
        Stage curStage = (Stage) chosen.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory =
                directoryChooser.showDialog(curStage);

        if(selectedDirectory == null){
            destinationLocationTextField.setText("No Directory selected");
        }else{
            destinationLocationTextField.setText(selectedDirectory.getAbsolutePath());
        }
    }
}
