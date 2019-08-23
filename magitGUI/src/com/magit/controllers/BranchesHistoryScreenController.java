package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.PreviousCommitsLimitExceededException;
import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.utils.compare.Delta;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.SortedSet;

public class BranchesHistoryScreenController implements BasicController {
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

    public void setEditedListView(ListView<Label> editedListView) {
        this.editedListView = editedListView;
    }

    public void setNewListView(ListView<Label> newListView) {
        this.newListView = newListView;
    }

    public void setDeletedListView(ListView<Label> deletedListView) {
        this.deletedListView = deletedListView;
    }

    public void setCurCommitSha1Label(String curCommitSha1Label) {
        this.curCommitSha1Label.setText(curCommitSha1Label);
    }

    public void setCommitMessageLabel(String commitMessageLabel) {
        this.commitMessageLabel.setText(commitMessageLabel);
    }

    public void setCreatorLabel(String creatorLabel) {
        this.creatorLabel.setText(creatorLabel);
    }

    public void setLastCommit1Label(String lastCommit1Label) {
        this.lastCommit1Label.setText(lastCommit1Label);
    }

    public void setLastCommit2Label(String lastCommit2Label) {
        this.lastCommit2Label.setText(lastCommit2Label);
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

    public void showDifferencesBetweenCommitAndFirstParent() {
        try {
            Map<FileStatus, SortedSet<Delta.DeltaFileItem>> differencesBetweenTwoCommits = engine.getDifferencesBetweenTwoCommits(curCommitSha1Label.getText(), lastCommit1Label.getText());
            editedListView.getItems().clear();
            deletedListView.getItems().clear();
            newListView.getItems().clear();
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

    private void createDiffLabels(Delta.DeltaFileItem item, ListView<Label> editedFilesListView) {
        Label itemLocation = new Label(item.getFullPath());
        String lastModifier = item.getLastUpdater();
        String commitDate = item.getLastModified();
        itemLocation.setTooltip(new Tooltip(String.format("Location: %s%sFile name: %s%sLast modifier: %s%sCommit date: %s",item.getFullPath(),System.lineSeparator(),item.getFileName(), System.lineSeparator(), lastModifier, System.lineSeparator(),commitDate)));
        editedFilesListView.getItems().add(itemLocation);
    }

}

