package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.gui.PopupScreen;
import com.magit.logic.exceptions.BranchAlreadyExistsException;
import com.magit.logic.exceptions.BranchNotFoundException;
import com.magit.logic.exceptions.InvalidNameException;
import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.logic.system.MagitEngine;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class CreateBranchScreenController implements BasicController {

    @FXML
    private TextField branchNameTextField;

    @FXML
    private ComboBox<String> rtbComboBox;

    @FXML
    private CheckBox rtbCheckBox;

    @FXML
    private Button cancelButton;

    @FXML
    private Button createButton;

    @FXML
    private Label messageLabel;

    private Stage stage;
    private MagitEngine engine;
    private String sha1OfCommit;
    private Runnable refreshGraph;

    @FXML
    void onCancel(ActionEvent event) {
        ((Stage)cancelButton.getScene().getWindow()).close();
    }

    @FXML
    void onCheckBoxAction(ActionEvent event) {
        ArrayList<String> remoteBranchesListOfSha1 = null;
        try {
            remoteBranchesListOfSha1 = engine.getRemoteBranchesOfCommit(sha1OfCommit);
            rtbComboBox.setDisable(!rtbCheckBox.isSelected());
            rtbComboBox.getItems().clear();
            rtbComboBox.getItems().addAll(remoteBranchesListOfSha1);
            branchNameTextField.setDisable(rtbCheckBox.isSelected());
        } catch (BranchNotFoundException e) {
            messageLabel.setText(e.getMessage());
            rtbCheckBox.setSelected(false);
        }
    }

    @FXML
    void onCreate(ActionEvent event) {
        if(rtbCheckBox.isSelected()){
            try {
                String selectedRTB = rtbComboBox.getSelectionModel().getSelectedItem();
                engine.createNewBranch(selectedRTB.split("/")[1], sha1OfCommit,selectedRTB);
                messageLabel.setText("Remote branch created successfully!");
                Platform.runLater(refreshGraph);
            } catch (IOException | InvalidNameException | RepositoryNotFoundException | BranchAlreadyExistsException e) {
                messageLabel.setText(e.getMessage());
            }
        }else{
            try {
                engine.createNewBranch(branchNameTextField.getText(), sha1OfCommit);
                messageLabel.setText("Branch created successfully!");
                Platform.runLater(refreshGraph);
            } catch (IOException | RepositoryNotFoundException | InvalidNameException | BranchAlreadyExistsException e) {
                messageLabel.setText(e.getMessage());
            }
        }
    }

    @FXML
    void onRTBComboClicked(MouseEvent event) {
       // ArrayList<String> remoteBranchesListOfSha1 = null;
       // try {
       //     remoteBranchesListOfSha1 = engine.getRemoteBranchesOfCommit(sha1OfCommit);
       //     rtbComboBox.getItems().addAll(remoteBranchesListOfSha1);
       // } catch (BranchNotFoundException e) {
       //     showErrorMessage(e.getMessage());
       // }

    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setEngine(MagitEngine engine) {
        this.engine = engine;
    }

    public void setSha1OfCommit(String sha1OfCommit) {
        this.sha1OfCommit = sha1OfCommit;
    }

    public void setRefreshGraph(Runnable refreshGraph) {
        this.refreshGraph = refreshGraph;
    }
}
