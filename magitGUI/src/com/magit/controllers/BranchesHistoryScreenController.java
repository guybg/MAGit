package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.PreviousCommitsLimitExceededException;
import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.utils.compare.Delta;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedSet;

public class BranchesHistoryScreenController implements BasicController, Initializable {
    private Stage stage;
    private MagitEngine engine;
    @FXML
    private ScrollPane scrollPaneContainer;

    @FXML
    private ListView<Label> editedListView;

    @FXML
    private ListView<Label> newListView;

    @FXML
    private ListView<Label> deletedListView;

    @FXML
    private Label curCommitSha1Label;

    @FXML
    private Label commitMessageLabel;

    @FXML
    private Label creatorLabel;

    @FXML
    private Label lastCommit1Label;

    @FXML
    private Label lastCommit2Label;

    @FXML
    private ComboBox<Label> switchDiffComboBox;

    private StringProperty chosenFather;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chosenFather = new SimpleStringProperty();
        chosenFather.addListener((observable, oldValue, newValue) -> {
                    switchDiffComboBox.promptTextProperty().setValue(String.format("Current commit vs %s", chosenFather.getValue()));
                    showDifferencesBetweenCommitAndChosenParent(chosenFather.getValue());
                }
        );
    }

    public void setEditedListView(ListView<Label> editedListView) {
        this.editedListView = editedListView;
    }

    public void setNewListView(ListView<Label> newListView) {
        this.newListView = newListView;
    }

    public void setDeletedListView(ListView<Label> deletedListView) {
        this.deletedListView = deletedListView;
    }

    public void setCurCommitSha1Label(String curCommitSha1) {
        this.curCommitSha1Label.setText(curCommitSha1);
    }

    public void setCommitMessageLabel(String commitMessage) {
        this.commitMessageLabel.setText(commitMessage);
    }

    public void setCreatorLabel(String creator) {
        this.creatorLabel.setText(creator);
    }

    public void setLastCommit1Label(String lastCommit1) {
        this.lastCommit1Label.setText(lastCommit1);
        this.chosenFather.setValue(lastCommit1);
    }

    @FXML
    private void onComboBoxClicked(MouseEvent event) {
        this.switchDiffComboBox.getItems().clear();
        createComboLabel(lastCommit1Label);
        if(!lastCommit2Label.textProperty().getValue().equals("")) {
            createComboLabel(lastCommit2Label);
        }
    }

    private void createComboLabel(Label lastCommitLabel) {
        Label comboLabel = new Label();
        comboLabel.textProperty().bind(Bindings.format("%s vs %s", "Current commit", lastCommitLabel.textProperty()));
        comboLabel.setTextFill(Color.BLACK);
        comboLabel.onMouseClickedProperty().addListener((observable, oldValue, newValue) -> {
            chosenFather.setValue(lastCommitLabel.getText());
        });
        this.switchDiffComboBox.getItems().add(comboLabel);
    }

    public void setLastCommit2Label(String lastCommit2) {
        this.lastCommit2Label.setText(lastCommit2);
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public MagitEngine getEngine() {
        return engine;
    }

    @Override
    public void setEngine(MagitEngine engine) {
        this.engine = engine;
    }

    private void updateDifferences(String parentCommitLabel) {
        editedListView.getItems().clear();
        deletedListView.getItems().clear();
        newListView.getItems().clear();
        try {
            Map<FileStatus, SortedSet<Delta.DeltaFileItem>> differencesBetweenTwoCommits = engine.getDifferencesBetweenTwoCommits(curCommitSha1Label.getText(), parentCommitLabel);

            for (Map.Entry<FileStatus, SortedSet<Delta.DeltaFileItem>> entry : differencesBetweenTwoCommits.entrySet()) {
                for (Delta.DeltaFileItem item : entry.getValue()) {
                    if (entry.getKey().equals(FileStatus.EDITED)) {
                        createDiffLabels(item, editedListView);
                    }
                    if (entry.getKey().equals(FileStatus.REMOVED)) {
                        createDiffLabels(item, deletedListView);
                    }
                    if (entry.getKey().equals(FileStatus.NEW)) {
                        createDiffLabels(item, newListView);
                    }
                }
            }
        } catch (IOException | ParseException | RepositoryNotFoundException | PreviousCommitsLimitExceededException e) {
                e.printStackTrace();
        }
    }

    public void showDifferencesBetweenCommitAndChosenParent(String parentName) {
        updateDifferences(parentName);
    }
    private void createDiffLabels(Delta.DeltaFileItem item, ListView<Label> editedFilesListView) {
        Label itemLocation = new Label(item.getFullPath());
        String lastModifier = item.getLastUpdater();
        String commitDate = item.getLastModified();
        itemLocation.setTooltip(new Tooltip(String.format("Location: %s%sFile name: %s%sLast modifier: %s%sCommit date: %s",item.getFullPath(),System.lineSeparator(),item.getFileName(), System.lineSeparator(), lastModifier, System.lineSeparator(),commitDate)));
        editedFilesListView.getItems().add(itemLocation);
    }



}

