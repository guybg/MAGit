package com.magit.controllers;


import com.magit.controllers.interfaces.BasicController;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.objects.ConflictItem;
import com.magit.logic.system.objects.FileItemInfo;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ConflictItemSolveScreenController implements BasicController {
    Stage stage;
    MagitEngine engine;
    ConflictItem conflictItem;
    BooleanProperty booleanProperty;


    @FXML
    private TextArea oursTextArea;

    @FXML
    private TextArea ancestorTextArea;

    @FXML
    private TextArea theirsTextArea;

    @FXML
    private TextArea mergeResultTextArea;

    @FXML
    private Button saveButton;

    @FXML
    void onSaveConflictChanges(ActionEvent event) {
        engine.updateSolvedConflict(conflictItem.getLocation(), conflictItem.getFileName(), mergeResultTextArea.getText().replaceAll("\n", System.getProperty("line.separator")));
        booleanProperty.setValue(true);
        ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
    }
    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setEngine(MagitEngine engine) {
        this.engine = engine;
    }

    public void setConflictItem(ConflictItem conflictItem) {
        this.conflictItem = conflictItem;
    }

    public void setBooleanProperty(BooleanProperty booleanProperty) {
        this.booleanProperty = booleanProperty;
    }

    public void init(){
        if(conflictItem != null){
            FileItemInfo ours = conflictItem.getOurs();
            if(ours!=null){
                oursTextArea.setText(ours.getFileContent());
            }
            FileItemInfo theirs = conflictItem.getTheirs();
            if(theirs!=null){
                theirsTextArea.setText(theirs.getFileContent());
            }
            FileItemInfo ancestor = conflictItem.getAncestor();
            if(ancestor!=null){
                ancestorTextArea.setText(ancestor.getFileContent());
            }
        }
    }
}
