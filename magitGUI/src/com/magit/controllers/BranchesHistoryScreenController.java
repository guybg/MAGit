package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.PreviousCommitsLimitExceededException;
import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.visual.node.CommitNode;
import com.sun.javafx.beans.IDProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedSet;

public class BranchesHistoryScreenController implements BasicController, Initializable {
    private Stage stage;
    private MagitEngine engine;
    public BooleanProperty focusChanged = new SimpleBooleanProperty();
    @FXML
    public ScrollPane scrollPaneContainer;

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
    private Label creationDateLabel;

    @FXML
    private Label creatorLabel;

    @FXML
    private Label allBranchesLabel;

    @FXML
    private Label allBranchesTitleLabel;

    @FXML
    private Hyperlink lastCommit1HyperLink;

    @FXML
    private Hyperlink lastCommit2HyperLink;

    private CommitNode lastCommit1Node;
    private CommitNode lastCommit2Node;
    @FXML
    private ComboBox<Label> switchDiffComboBox;

    private StringProperty chosenFather;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chosenFather = new SimpleStringProperty();
        chosenFather.addListener((observable, oldValue, newValue) -> {
            if(!lastCommit1HyperLink.getText().isEmpty())
                switchDiffComboBox.promptTextProperty().setValue(String.format("Current commit vs %s", chosenFather.getValue()));
            else
                switchDiffComboBox.promptTextProperty().setValue("");
            showDifferencesBetweenCommitAndChosenParent(chosenFather.getValue());
        });
    }

    public void setCurCommitSha1Label(String curCommitSha1) {
        this.curCommitSha1Label.setText(curCommitSha1);
        setCommitLabelToolTip(curCommitSha1Label);
    }

    public void setCommitMessageLabel(String commitMessage) {
        this.commitMessageLabel.setText(commitMessage);
        setCommitLabelToolTip(commitMessageLabel);
    }

    public void setCreatorLabel(String creator) {
        this.creatorLabel.setText(creator);
        setCommitLabelToolTip(creatorLabel);
    }

    public void setLastCommit1HyperLink(String lastCommit1) {
        lastCommit1HyperLink.setVisible(!lastCommit1.isEmpty());
        this.lastCommit1HyperLink.setText(lastCommit1);
        this.chosenFather.setValue(lastCommit1);
    }

    public void setLastCommit2HyperLink(String lastCommit2) {
        lastCommit2HyperLink.setVisible(!lastCommit2.isEmpty());
        this.lastCommit2HyperLink.setText(lastCommit2);
    }

    @FXML
    private void onComboBoxClicked(MouseEvent event) {
        this.switchDiffComboBox.getItems().clear();
        createComboLabel(lastCommit1HyperLink);
        if(!lastCommit2HyperLink.textProperty().getValue().equals("")) {
            createComboLabel(lastCommit2HyperLink);
        }
    }

    public void setCreationDateLabel(String creationDate) {
        this.creationDateLabel.setText(creationDate);
        setCommitLabelToolTip(creationDateLabel);
    }

    private void createComboLabel(Hyperlink lastCommitLabel) {
        Label comboLabel = new Label();
        comboLabel.textProperty().bind(Bindings.when(lastCommit1HyperLink.textProperty().isNotEqualTo("")).then(Bindings.format("%s vs %s", "Current commit", lastCommitLabel.textProperty())).otherwise(""));
        comboLabel.setTextFill(Color.BLACK);
        comboLabel.onMouseClickedProperty().addListener((observable, oldValue, newValue) -> {
            chosenFather.setValue(lastCommitLabel.getText());
        });
        this.switchDiffComboBox.getItems().add(comboLabel);
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

    @FXML
    void onClickFirstParent(MouseEvent event) {
        if(lastCommit1Node!=null)
            lastCommit1Node.showMe();
    }

    @FXML
    void onClickSecondParent(MouseEvent event) {
        if(lastCommit2Node!=null)
            lastCommit2Node.showMe();
    }

    public void setLastCommit1Node(CommitNode lastCommit1Node) {
        this.lastCommit1Node = lastCommit1Node;
    }

    public void setLastCommit2Node(CommitNode lastCommit2Node) {
        this.lastCommit2Node = lastCommit2Node;
    }

    public void setAllBranchesLabel(String allBranches, int numberOfBranches) {
        this.allBranchesLabel.setText(allBranches);
        this.allBranchesTitleLabel.setText("in " + numberOfBranches + " branches:");
        setCommitLabelToolTip(allBranchesLabel);
    }

    private void setCommitLabelToolTip(Label commitLabel){
        commitLabel.setTooltip(new Tooltip(commitLabel.getText()));
    }
}

