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
import javafx.scene.control.CheckBox;
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
    private CheckBox deleteCheckBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button minimizeButton;

    @FXML
    private Button closeButton;

    @FXML
    void onClose(ActionEvent event) {
        Button closeButton = (Button)event.getSource();
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void onMinimize(ActionEvent event) {
        Button minimizeButton = (Button)event.getSource();
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    void onSaveConflictChanges(ActionEvent event) {
        engine.updateSolvedConflict(conflictItem.getLocation(), mergeResultTextArea.getText().replaceAll("\n", System.getProperty("line.separator")), deleteCheckBox.isSelected());
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
        oursTextArea.setDisable(true);
        theirsTextArea.setDisable(true);
        ancestorTextArea.setDisable(true);
        
        if(conflictItem != null){
            FileItemInfo ours = conflictItem.getOurs();
            if(ours!=null){
                oursTextArea.setDisable(false);
                oursTextArea.setText(ours.getFileContent());
            }
            FileItemInfo theirs = conflictItem.getTheirs();
            if(theirs!=null){
                theirsTextArea.setDisable(false);
                theirsTextArea.setText(theirs.getFileContent());
            }
            FileItemInfo ancestor = conflictItem.getAncestor();
            if(ancestor!=null){
                ancestorTextArea.setDisable(false);
                ancestorTextArea.setText(ancestor.getFileContent());
            }
            if(ours == null || theirs == null || ancestor == null)
                deleteCheckBox.setVisible(true);
            mergeResultTextArea.disableProperty().bind(deleteCheckBox.selectedProperty());
        }
    }
}
