package com.magit.controllers;


import com.magit.controllers.interfaces.BasicController;
import com.magit.controllers.interfaces.BasicPopupScreenController;
import com.magit.gui.PopupScreen;
import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.objects.FileItemInfo;
import com.magit.logic.utils.compare.Delta;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.control.CheckBox;
import javafx.stage.*;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.function.Consumer;


public class MainScreenController implements Initializable, BasicController {

    private MagitEngine engine;
    private Stage stage;
    private StringProperty userNameProperty;
    private StringProperty repositoryNameProperty;
    private StringProperty branchNameProperty;
    private StringProperty repositoryPathProperty;

    private double xOffset = 0;
    private double yOffset = 0;


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setEngine(MagitEngine engine) {
        this.engine = engine;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        menuItem1Label.prefWidthProperty().bind(currentRepositoryMenuButton.widthProperty().subtract(14));
        switchUserLabel.prefWidthProperty().bind(userNameMenuButton.widthProperty().subtract(78));
        userNameProperty = new SimpleStringProperty();
        repositoryPathProperty = new SimpleStringProperty();
        userNameProperty.setValue("Administrator");
        userNameMenuButton.textProperty().bind(userNameProperty);
        if(repositoryNameProperty == null) {
            repositoryNameProperty = new SimpleStringProperty();
            repositoryNameProperty.setValue("");
        }

        menuButtonRepositoryNameLabel.textProperty().bind(Bindings
                .when(repositoryNameProperty.isNotEqualTo(""))
                .then(repositoryNameProperty)
                .otherwise("No repository"));
        branchNameProperty = new SimpleStringProperty();
        middleAnchorPane.minWidthProperty().bind(middleHSplitPane.widthProperty().divide(2));
        menuButtonBranchNameLabel.textProperty().bind(Bindings
                .when(branchNameProperty.isNotEqualTo(""))
                .then(branchNameProperty)
                .otherwise("No branch"));
        commitToLeftDownButton.textProperty().bind(Bindings.format("%s %s", "Commit to", branchNameProperty));
        branchNameProperty.addListener((observable, oldValue, newValue) -> updateDifferences());
        commitToLeftDownButton.setDisable(true);
        resetBranchMenuItem.setDisable(true);
        deleteBranchMenuItem.setDisable(true);
        newBranchMenuItem.setDisable(true);
        commitHistoryMenuItem.setDisable(true);
        repositoryNameProperty.addListener((observable, oldValue, newValue) -> {
            commitToLeftDownButton.setDisable(false);
            loadBranchesToUserInterface();
            resetBranchMenuItem.setDisable(false);
            deleteBranchMenuItem.setDisable(false);
            newBranchMenuItem.setDisable(false);
            commitHistoryMenuItem.setDisable(false);
            repositoryPathProperty.setValue(engine.guiGetRepositoryPath());
        });
        repositoryPathProperty.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                currentRepositoryMenuButton.tooltipProperty().setValue(new Tooltip(repositoryPathProperty.getValue()));
            }
        });
        showWelcomeNode();
    }
    @FXML private ListView<?> branchesListView;
    @FXML private Label menuButtonBranchNameLabel;
    @FXML private Label menuButtonRepositoryNameLabel;
    @FXML private AnchorPane anchorPane;
    @FXML private MenuBar menuBar;
    @FXML private Menu fileMenu;
    @FXML private MenuItem newRepositoryMenuItem;
    @FXML private Menu openRepositoryMenu;
    @FXML private MenuItem browseMenuItem;
    @FXML private MenuItem browseXMLFileMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private Menu viewMenu;
    @FXML private MenuItem workingCopyStatusMenuItem;
    @FXML private Menu commitMenu;
    @FXML private MenuItem newCommitMenuItem;
    @FXML private MenuItem commitHistoryMenuItem;
    @FXML private Menu repositoryMenu;
    @FXML private MenuItem pushMenuItem;
    @FXML private MenuItem pullMenuItem;
    @FXML private MenuItem exportXmlMenuItem;
    @FXML private Menu branchMenu;
    @FXML private MenuItem newBranchMenuItem;
    @FXML private MenuItem deleteBranchMenuItem;
    @FXML private MenuItem resetBranchMenuItem;
    @FXML private MenuItem mergeMenuItem;
    @FXML private Menu helpMenu;
    @FXML private MenuItem aboutMenuItem;
    @FXML private HBox windowCloseAndMinimizeHbox;
    @FXML private Button minimizeButton;
    @FXML private Button closeButton;
    @FXML private RowConstraints buttonbarGridLine;
    @FXML private MenuButton currentRepositoryMenuButton;
    @FXML private MenuItem menuItem1;
    @FXML private Label menuItem1Label;
    @FXML private MenuButton currentBranchMenuButton;
    @FXML private MenuButton userNameMenuButton;
    @FXML private MenuItem SwitchUserMenuItem1;
    @FXML private Label switchUserLabel;
    @FXML private TextArea commitMessageTextArea;
    @FXML private Button commitToLeftDownButton;
    @FXML private Label commitDateLeftDownLabel;
    @FXML private Label commitMessageLeftDownLabel;
    @FXML private TitledPane editedTitlePane;
    @FXML private ListView<Label> editedFilesListView;
    @FXML private TitledPane deletedTitlePane;
    @FXML private ListView<Label> deletedFilesListView;
    @FXML private TitledPane newFilesTitlePane;
    @FXML private ListView<Label> newFilesListView;
    @FXML private Button openChangesRefreshButton;
    @FXML private CheckBox checkBox;
    @FXML private AnchorPane middleAnchorPane;
    @FXML private SplitPane middleHSplitPane;
    @FXML private Label moveScreenLabel;

    private ObservableList<FileItemInfo> fileItemInfos;

    @FXML void onExitApplication(ActionEvent event) {
        stage.close();
    }

    @FXML
    void OnCloseButtonAction(ActionEvent event) {
        Button closeButton = (Button)event.getSource();
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void OnMinimizeButtonAction(ActionEvent event) {
        Button minimizeButton = (Button)event.getSource();
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    void onClick(ActionEvent event) {

    }

    @FXML
    void OnMouseDragged(MouseEvent event) {
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    @FXML
    void OnMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }


    @FXML
    void onClickCommitButton(MouseEvent event) {
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
                PopupScreen popupScreen = new PopupScreen(stage, engine);
                popupScreen.createNotificationPopup(event12 -> {
                }, false, "Commit creation notification", "Files commited successfully", "Close");
                updateDifferences();
            }catch (IOException e){
                e.printStackTrace();
            }
        },commitMessageTextArea.getText());
    }

    @FXML
    void onCommitMessageTextAreaChanged(InputMethodEvent event) {
        System.out.println();
    }

    @FXML
    void onCurrentBranchMenuButtonClicked(MouseEvent event) {
        loadBranchesToUserInterface();
    }

    void loadBranchesToUserInterface() {
        if(repositoryNameProperty.getValue().equals("")) return;
        currentBranchMenuButton.getItems().clear();
        branchNameProperty.setValue(engine.getHeadBranchName());
        ArrayList<String> branchesNames = engine.getBranchesName();
        for (String branchName : branchesNames) {
            MenuItem menuItem = new MenuItem();
            menuItem.textProperty().setValue(branchName);
            menuItem.setGraphic(new Label());
            ((Label)menuItem.getGraphic()).prefWidthProperty().bind(currentBranchMenuButton.widthProperty().subtract(54));
            menuItem.setOnAction(event -> {
                try {
                    onBranchButtonMenuItemClick(menuItem.getText());
                } catch (ParseException | RepositoryNotFoundException | InvalidNameException | BranchNotFoundException e) {
                    e.printStackTrace();
                }
            });
            currentBranchMenuButton.getItems().add(menuItem);
        }
    }

    @FXML
    void onResetBranchMenuItemClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/resetBranchScreen.fxml"));
        Parent layout = loader.load();
        ResetBranchScreenController resetBranchScreenController = loader.getController();
        PopupScreen popupScreen = new PopupScreen(stage,engine);
        popupScreen.createPopup(layout, resetBranchScreenController);
        updateDifferences();
    }

    void onBranchButtonMenuItemClick(String branchName) throws ParseException, RepositoryNotFoundException,
            InvalidNameException, BranchNotFoundException{
        String headMessage = "There are unsaved changes";
        String bodyMessage = "are you sure you want to switch branch?";
        try {
            engine.pickHeadBranch(branchName);
            branchNameProperty.setValue(branchName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UncommitedChangesException e) {
            BasicPopupScreenController controller = event1 -> {
                try {
                    engine.forcedChangeBranch(branchName);
                    branchNameProperty.setValue(branchName);
                } catch (ParseException | IOException | PreviousCommitsLimitExceededException ignored) {}
                Button button = (Button)event1.getSource();
                ((Stage)(button.getScene().getWindow())).close();
            };
            try {
                PopupScreen popupScreen = new PopupScreen(stage,engine);
                popupScreen.createNotificationPopup(controller, true, headMessage, bodyMessage, "Cancel");
            } catch(IOException ignored) {}
        } catch (PreviousCommitsLimitExceededException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openUserNameChangeScreen(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/generalScreenEnterString.fxml"));
        Parent layout = loader.load();
        GeneralScreenEnterStringController userNameController =
                getGeneralScreen(loader, "Switch User Name", "User Name:");
        userNameController.setController(buttonEvent -> {
            try {
                String fieldValue = userNameController.getTextFieldValue();
                engine.updateUserName(fieldValue);
                userNameProperty.setValue(fieldValue);
                ((Stage)((Button)buttonEvent.getSource()).getScene().getWindow()).close();
            } catch (InvalidNameException e) {
                userNameController.setError(e.getMessage());
            }
        });
        PopupScreen popupScreen = new PopupScreen(stage,engine);
        popupScreen.createPopup(layout,userNameController);
    }

    @FXML
    void openNewRepositoryScreenAction() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/createNewRepositoryScreen.fxml"));
        Parent layout = loader.load();
        CreateNewRepositoryScreenController createNewRepositoryScreenController = loader.getController();
        createNewRepositoryScreenController.setRepositoryNameProperty(repositoryNameProperty);
        createNewRepositoryScreenController.bindings();
        PopupScreen popupScreen = new PopupScreen(stage,engine);
        popupScreen.createPopup(layout, createNewRepositoryScreenController);
        //events on properties handles branches load, diff loads
    }

    void showWelcomeNode(){
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/welcomenode.fxml"));
        Node welcomeNode = null;
        try {
            welcomeNode = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        WelcomeNodeController controller = loader.getController();
        controller.setOnLoad(() -> {
            try {
                openRepositoryFromFolderChooserAction();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        controller.setOnCreate(() -> {
            try {
                openNewRepositoryScreenAction();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        controller.setOnLoadXml(() -> {
            try {
                openRepositoryFromXmlAction();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        AnchorPane.setBottomAnchor(welcomeNode, 0.0);
        AnchorPane.setLeftAnchor(welcomeNode, 0.0);
        AnchorPane.setRightAnchor(welcomeNode, 0.0);
        AnchorPane.setTopAnchor(welcomeNode, 0.0);
        middleAnchorPane.getChildren().add(welcomeNode);
    }

    @FXML
    void onGetCommitHistoryClicked(ActionEvent event) {
        engine.guiCollectCommitHistoryInfo(new Consumer<ObservableList<FileItemInfo>>() {
            @Override
            public void accept(ObservableList<FileItemInfo> fileItemInfos) {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/com/magit/resources/tableScreen.fxml"));
                Node table = null;
                try {
                    table = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                TableScreenController tableScreenController = loader.getController();
                tableScreenController.init(fileItemInfos);
                AnchorPane.setBottomAnchor(table, 0.0);
                AnchorPane.setLeftAnchor(table, 0.0);
                AnchorPane.setRightAnchor(table, 0.0);
                AnchorPane.setTopAnchor(table, 0.0);
                middleAnchorPane.getChildren().add(table);
            }
        }, s -> {
            PopupScreen popupScreen = new PopupScreen(stage, engine);
            try {
                popupScreen.createNotificationPopup(null, false, "Commit Notification", s, "Close");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }


    @FXML
    void openRepositoryFromXmlAction() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/importXmlScreen.fxml"));
        Parent layout = loader.load();
        XmlImportController xmlController = loader.getController();
        xmlController.setStage(stage);
        xmlController.setEngine(engine);
        PopupScreen popupScreen = new PopupScreen(stage,engine);
        popupScreen.createPopup(layout, xmlController);
    }


    private void importRepositoryFromXml(File xmlFile, PopupScreen popupScreen) throws IOException {
        if(null == xmlFile)
            return;

        try{
            engine.loadHeadBranchCommitFiles(xmlFile.getAbsolutePath(), false);
        } catch (IllegalPathException | ParseException | XmlFileException | PreviousCommitsLimitExceededException | JAXBException | IOException e) {
            popupScreen.createNotificationPopup(null, false, "Repository from XML notification", e.getMessage(),"Close");
        } catch (RepositoryAlreadyExistsException e) {
            BasicPopupScreenController basicPopupScreenController1 = event1 -> {
                try {
                    engine.loadHeadBranchCommitFiles(xmlFile.getAbsolutePath(),true);
                    Button chosen = (Button) event1.getSource();
                    Stage curStage = (Stage) chosen.getScene().getWindow();
                    curStage.close();
                    popupScreen.createNotificationPopup(null, false, "Repository from XML notification", "Repository created successfully.","Close");
                } catch (JAXBException | IOException | ParseException | PreviousCommitsLimitExceededException | XmlFileException | IllegalPathException | RepositoryAlreadyExistsException ex) {
                    try {
                        popupScreen.createNotificationPopup(event11 -> {
                            Button chosen = (Button) event11.getSource();
                            Stage curStage = (Stage) chosen.getScene().getWindow();
                            curStage.close();
                        },false,"Repository from XML error",e.getMessage(),"Close");
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }
                }
            };
            popupScreen.createNotificationPopup(basicPopupScreenController1, true,"Repository already exists notification","Would you like to replace current repository with XML repository?","Cancel");
        }
        repositoryNameProperty.setValue(engine.getRepositoryName());
    }

    @FXML
    void openRepositoryFromFolderChooserAction() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory =
                directoryChooser.showDialog(stage);
        if(selectedDirectory == null)
            return;
        try {
            engine.switchRepository(selectedDirectory.getAbsolutePath());
            repositoryNameProperty.setValue(engine.getRepositoryName());
            //events on properties handles branches load, diff loads //loadBranchesToUserInterface();
        } catch (IOException | ParseException | RepositoryNotFoundException e) {
            PopupScreen popupScreen = new PopupScreen(stage,engine);
            popupScreen.createNotificationPopup(null,false,"Repository creation notification",e.getMessage(),"Close");
        }
    }

    @FXML
    void onOpenChangesRefreshButtonClicked(MouseEvent event) {
        updateDifferences();
    }

    void updateDifferences(){
        Integer editedCount = 0, deletedCount = 0, newCount = 0;
        editedFilesListView.getItems().clear();
        deletedFilesListView.getItems().clear();
        newFilesListView.getItems().clear();
        try {
            Map<FileStatus, SortedSet<Delta.DeltaFileItem>> openChanges =  engine.getWorkingCopyStatusMap();
            for(Map.Entry<FileStatus, SortedSet<Delta.DeltaFileItem>> entry : openChanges.entrySet()){
                for(Delta.DeltaFileItem item : entry.getValue()) {
                    if (entry.getKey().equals(FileStatus.EDITED)) {
                        editedCount = createDiffLabels(editedCount, item, editedFilesListView);
                    }
                    if (entry.getKey().equals(FileStatus.REMOVED)) {
                        deletedCount = createDiffLabels(deletedCount, item, deletedFilesListView);
                    }
                    if(entry.getKey().equals(FileStatus.NEW)) {
                        newCount = createDiffLabels(newCount, item, newFilesListView);
                    }
                }
            }
            editedTitlePane.setText(editedCount.toString());
            deletedTitlePane.setText(deletedCount.toString());
            newFilesTitlePane.setText(newCount.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (RepositoryNotFoundException e) {
            BasicPopupScreenController controller = event1 -> {};
            try {
                PopupScreen popupScreen = new PopupScreen(stage,engine);
                popupScreen.createNotificationPopup(controller,false,"Refresh notification",e.getMessage(),"Close");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (PreviousCommitsLimitExceededException e) {
            e.printStackTrace();
        }
    }

    private Integer createDiffLabels(Integer editedCount, Delta.DeltaFileItem item, ListView<Label> editedFilesListView) {
        Label itemLocation = new Label(item.getFullPath());
        String lastModifier = item.getLastUpdater();
        String commitDate = item.getLastModified();
        if(item.getLastUpdater().equals("")) {
            lastModifier = userNameProperty.getValue();
            commitDate = "not committed";
        }
        itemLocation.setTooltip(new Tooltip(String.format("Location: %s%sFile name: %s%sLast modifier: %s%sCommit date: %s",item.getFullPath(),System.lineSeparator(),item.getFileName(), System.lineSeparator(), lastModifier, System.lineSeparator(),commitDate)));
        editedFilesListView.getItems().add(itemLocation);
        editedCount++;
        return editedCount;
    }

    @FXML
    private void onDeleteBranchClick() throws IOException  {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/generalScreenEnterString.fxml"));
        Parent layout = loader.load();
        GeneralScreenEnterStringController deleteBranchCotnroller =
                getGeneralScreen(loader, "Create New Branch", "Branch Name:");
        deleteBranchCotnroller.setController(event -> {
            String branchName = deleteBranchCotnroller.getTextFieldValue();
            try {
                try {
                    engine.deleteBranch(branchName);
                    ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
                } catch (IOException e) {
                    deleteBranchCotnroller.setError("Please enter valid name.");
                }
            }
             catch (ActiveBranchDeletedException ex){
                 deleteBranchCotnroller.setError("Can't delete active branch");
             } catch (BranchNotFoundException ex) {
                deleteBranchCotnroller.setError("Branch doesn't exist, or branch name is written wrong.");
            }
            catch (RepositoryNotFoundException ex) {
                deleteBranchCotnroller.setError("No repository loaded.");
            }
        });
        PopupScreen popupScreen = new PopupScreen(stage,engine);
        popupScreen.createPopup(layout, deleteBranchCotnroller);
    }

    @FXML
    private void onNewBranchClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/generalScreenEnterString.fxml"));
        Parent layout = loader.load();
        GeneralScreenEnterStringController newBranchController =
                getGeneralScreen(loader, "Create new branch", "Branch name:");
        newBranchController.setCheckBoxVisible();
        PopupScreen popupScreen = new PopupScreen(stage,engine);
        newBranchController.setController(event -> {
            String branchName = newBranchController.getTextFieldValue();
            try {
                engine.createNewBranch(branchName);
                newBranchController.setError("Branch created successfully!");
                newBranchController.acceptButtonDisabled(true);
                if (newBranchController.getCheckBoxValue()) {
                    try {
                        engine.pickHeadBranch(branchName);
                        branchNameProperty.setValue(branchName);
                    } catch (ParseException | BranchNotFoundException | PreviousCommitsLimitExceededException e) {
                        newBranchController.setError(e.getMessage());
                    } catch (UncommitedChangesException e) {
                        popupScreen.createNotificationPopup(cEvent -> {
                            forceChangeBranch(branchName);
                            branchNameProperty.setValue(branchName);
                            ((Stage)((Button)cEvent.getSource()).getScene().getWindow()).close();
                            },true, "Are you sure?","There are unsaved changes, switching branch may cause lose of data.", "Cancel");
                    }
                }
            } catch (IOException | InvalidNameException | RepositoryNotFoundException | BranchAlreadyExistsException e ) {
                newBranchController.setError(e.getMessage());
            }
        });
        popupScreen.createPopup(layout, newBranchController);
    }

    private void forceChangeBranch(String branchName) {
        try {
            engine.forcedChangeBranch(branchName);
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (PreviousCommitsLimitExceededException ex) {
            ex.printStackTrace();
        }
    }

    private GeneralScreenEnterStringController getGeneralScreen(FXMLLoader loader ,String headLabelValue, String keyLabelValue)
            throws IOException {
        GeneralScreenEnterStringController generalController = loader.getController();
        generalController.setHeadLabel(headLabelValue);
        generalController.setKeyLabel(keyLabelValue);

        return generalController;
    }


}
