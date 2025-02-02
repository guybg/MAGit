package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.gui.PopupScreen;
import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.ConflictItem;
import com.magit.logic.system.objects.FileItemInfo;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;

public class MergeScreenController implements BasicController, Initializable {
    private Stage stage;
    private MagitEngine engine;
    private boolean preReadyMerge = false;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mergeButton.setDisable(true);
        branchToMergeWithComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null){
                mergeButton.setDisable(true);
            }else{
                mergeButton.setDisable(false);
            }
        });
    }

    @FXML
    private ListView<Label> openChangesListView;

    @FXML
    private TextArea mergeCommitMessageTextArea;

    @FXML
    private ListView<Label> conflictsListView;

    @FXML
    private Button commitButton;

    @FXML
    private ComboBox<String> branchToMergeWithComboBox;

    @FXML
    private Button mergeButton;

    @FXML
    void onCommit(MouseEvent event) {
        engine.guiCommit(s -> {
            PopupScreen popupScreen = new PopupScreen(stage, engine);
            try {
                popupScreen.createNotificationPopup(event1 -> {
                }, false, "Commit creation notification", s, "Close");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, () -> {
            try {
                PopupScreen popupScreen = new PopupScreen(((Stage)(((Button)event.getSource()).getScene().getWindow())), engine);
                popupScreen.createNotificationPopup(event12 -> {
                }, false, "Commit creation notification", "Files committed successfully", "Close");
                ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
            }catch (IOException e){
                e.printStackTrace();
            }
        },mergeCommitMessageTextArea.getText());
    }

    @FXML
    void onMerge(MouseEvent event) {
        try {
            engine.merge(branchToMergeWithComboBox.getValue(), false);
            updateOpenChanges();
            updateConflicts();
            mergeButton.setDisable(true);
            branchToMergeWithComboBox.setDisable(true);
        } catch (UnhandledMergeException e) {
            unhandledMergeExceptionHandler(e.getMessage());
        } catch ( FastForwardException e) {
            fastForwardExceptionHandler(e.getMessage());
        } catch (MergeNotNeededException e){
            fastForwardExceptionHandler(e.getMessage());
            ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
        } catch (MergeException e) {
            mergeException(e.getMessage());
            ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
        } catch (UncommitedChangesException e) {
            PopupScreen popupScreen = new PopupScreen(stage,engine);
            popupScreen.showErrorMessage(e.getMessage());
            ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
        } catch (RepositoryNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateConflicts() {
        conflictsListView.getItems().clear();
        ArrayList<ConflictItem> conflicts = engine.getMergeConflicts();
        if(conflicts != null){
            for(ConflictItem conflict : conflicts){
                Label conflictLabel = new Label(conflict.getLocation());
                conflictLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        PopupScreen popupScreen = new PopupScreen(((Stage)((Label)event.getSource()).getScene().getWindow()), engine);
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(getClass().getResource("/com/magit/resources/fxml/conflictItemSolveScreen.fxml"));
                        try {
                            BooleanProperty solved = new SimpleBooleanProperty(false);
                            Parent layout = loader.load();
                            ConflictItemSolveScreenController conflictItemSolveScreenController = loader.getController();
                            conflictItemSolveScreenController.setConflictItem(conflict);
                            conflictItemSolveScreenController.setBooleanProperty(solved);
                            conflictItemSolveScreenController.init();
                            popupScreen.createPopup(layout, loader.getController());
                            if(solved.getValue().equals(true)){
                                conflictsListView.getItems().remove(conflictLabel);
                                Label newConflictSolvedItem = new Label(conflictLabel.getText() + "(NEW)");
                                openChangesListView.getItems().add(newConflictSolvedItem);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                conflictsListView.getItems().add(conflictLabel);
            }
        }
    }

    @FXML
    void onShowBranches(MouseEvent event) {
        branchToMergeWithComboBox.getItems().clear();
        Collection<Branch> branches = engine.getNonRemoteBranches();
        for(Branch branch: branches){
            String branchName = branch.getBranchName();
            branchToMergeWithComboBox.getItems().add(branchName);
        }

    }

    public void preReadyMerge(){
        if(engine.headBranchHasMergeOpenChanges())
            updateOpenChanges();
        if(engine.headBranchHasMergeConflicts())
            updateConflicts();
        mergeButton.setVisible(false);
        branchToMergeWithComboBox.setVisible(false);
        checkFastForward();
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void setEngine(MagitEngine engine) {
        this.engine = engine;
    }

    private void updateOpenChanges(){
        openChangesListView.getItems().clear();
        try {
            for(Map.Entry<FileStatus, ArrayList<FileItemInfo>> entry : engine.getMergeOpenChanges().entrySet()){
                for(FileItemInfo openChange : entry.getValue()){
                    Label openChangeLabel = new Label(openChange.getFileLocation() + "(" + entry.getKey() + ")");
                    //tooltip here
                    openChangesListView.getItems().add(openChangeLabel);
                }
            }
        } catch (PreviousCommitsLimitExceededException e) {
            e.printStackTrace();
        } catch (RepositoryNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void unhandledMergeExceptionHandler(String exceptionMessage){
        PopupScreen popupScreen = new PopupScreen(stage,engine);
        try {
            branchToMergeWithComboBox.setPromptText(engine.getMergedWithBranchNameFromUnhandledMerge());
            popupScreen.createNotificationPopup(null,false,"Unhandled merge",exceptionMessage,"Close");
            updateOpenChanges();
            updateConflicts();
            checkFastForward();
            branchToMergeWithComboBox.setPromptText(engine.getMergedWithBranchNameFromUnhandledMerge());
            mergeButton.setDisable(true);
            branchToMergeWithComboBox.setDisable(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void checkFastForward(){
        if(!engine.headBranchHasMergeConflicts() && !engine.headBranchHasMergeOpenChanges())
            mergeCommitMessageTextArea.setDisable(true);
    }
    private void fastForwardExceptionHandler(String exceptionMessage){
        PopupScreen popupScreen = new PopupScreen(stage,engine);
        mergeCommitMessageTextArea.setDisable(true);
        try {
            popupScreen.createNotificationPopup(null,false,"Fast forward notification",exceptionMessage,"Close");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void mergeException(String exceptionMessage){
        PopupScreen popupScreen = new PopupScreen(stage,engine);
        mergeCommitMessageTextArea.setDisable(true);
        try {
            popupScreen.createNotificationPopup(null,false,"Merge error notification",exceptionMessage,"Close");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
