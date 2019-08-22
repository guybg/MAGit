package com.magit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;

public class BranchesHistoryScreenController {

    @FXML
    private ScrollPane scrollPaneContainer;

    @FXML
    private ListView<?> editedListView;

    @FXML
    private ListView<?> newListView;

    @FXML
    private ListView<?> deletedListView;

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

    public void setEditedListView(ListView<?> editedListView) {
        this.editedListView = editedListView;
    }

    public void setNewListView(ListView<?> newListView) {
        this.newListView = newListView;
    }

    public void setDeletedListView(ListView<?> deletedListView) {
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
}
