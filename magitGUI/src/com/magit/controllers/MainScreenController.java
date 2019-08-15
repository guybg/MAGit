package com.magit.controllers;

import com.magit.controllers.interfaces.BasicController;
import com.magit.controllers.interfaces.BasicPopupScreenController;
import com.magit.gui.ResizeHelper;
import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.utils.compare.Delta;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
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

public class MainScreenController implements Initializable, BasicController {

    private MagitEngine engine;
    private Stage stage;
    private StringProperty userNameProperty;
    private StringProperty repositoryNameProperty;
    private StringProperty branchNameProperty;
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
        userNameProperty.setValue("Administrator");
        userNameMenuButton.textProperty().bind(Bindings.format("Hello, %s",userNameProperty));
        if(repositoryNameProperty == null) {
            repositoryNameProperty = new SimpleStringProperty();
            repositoryNameProperty.setValue("");
        }
        if (repositoryNameProperty.getValue().isEmpty()) repositoryNameProperty.setValue("No repository");
        //buttonbarGridLine.prefHeightProperty().bind(currentRepositoryMenuButton.heightProperty());
        currentRepositoryMenuButton.textProperty().bind(Bindings.format("Current Repository %s%s",System.lineSeparator(),repositoryNameProperty));
        branchNameProperty = new SimpleStringProperty();
        branchNameProperty.setValue("No branch");
        currentBranchMenuButton.textProperty().bind(Bindings.format(" Current branch%s %s", System.lineSeparator(),branchNameProperty));
    }

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private MenuBar menuBar;

    @FXML
    private Menu fileMenu;

    @FXML
    private MenuItem newRepositoryMenuItem;

    @FXML
    private Menu openRepositoryMenu;

    @FXML
    private MenuItem browseMenuItem;

    @FXML
    private MenuItem browseXMLFileMenuItem;

    @FXML
    private MenuItem exitMenuItem;

    @FXML
    private Menu viewMenu;

    @FXML
    private MenuItem workingCopyStatusMenuItem;

    @FXML
    private Menu commitMenu;

    @FXML
    private MenuItem newCommitMenuItem;

    @FXML
    private MenuItem commitHistoryMenuItem;

    @FXML
    private Menu repositoryMenu;

    @FXML
    private MenuItem pushMenuItem;

    @FXML
    private MenuItem pullMenuItem;

    @FXML
    private MenuItem exportXmlMenuItem;

    @FXML
    private Menu branchMenu;

    @FXML
    private MenuItem newBranchMenuItem;

    @FXML
    private MenuItem deleteBranchMenuItem;

    @FXML
    private MenuItem resetBranchMenuItem;

    @FXML
    private MenuItem mergeMenuItem;

    @FXML
    private Menu helpMenu;

    @FXML
    private MenuItem aboutMenuItem;

    @FXML
    private HBox windowCloseAndMinimizeHbox;

    @FXML
    private Button minimizeButton;

    @FXML
    private Button closeButton;

    @FXML
    private RowConstraints buttonbarGridLine;

    @FXML
    private MenuButton currentRepositoryMenuButton;

    @FXML
    private MenuItem menuItem1;

    @FXML
    private Label menuItem1Label;

    @FXML
    private MenuButton currentBranchMenuButton;

    @FXML
    private MenuButton userNameMenuButton;

    @FXML
    private MenuItem SwitchUserMenuItem1;

    @FXML
    private Label switchUserLabel;

    @FXML
    private TextArea commitMessageTextArea;

    @FXML
    private Button commitToLeftDownButton;

    @FXML
    private Label commitDateLeftDownLabel;

    @FXML
    private Label commitMessageLeftDownLabel;

    @FXML
    private TitledPane editedTitlePane;

    @FXML
    private VBox editedFilesVbox;

    @FXML
    private TitledPane deletedTitlePane;

    @FXML
    private VBox deletedFilesVbox;

    @FXML
    private TitledPane newFilesTitlePane;

    @FXML
    private VBox newFilesVbox;

    @FXML
    private Button openChangesRefreshButton;

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

    }

    @FXML
    void onCommitMessageTextAreaChanged(InputMethodEvent event) {

    }

    @FXML
    void onCurrentBranchMenuButtonClicked(MouseEvent event) {
        loadBranchesToUserInterface();
    }

    void loadBranchesToUserInterface() {
        currentBranchMenuButton.getItems().clear();
        branchNameProperty.setValue(engine.getHeadBranchName());
        ArrayList<String> branchesNames = engine.getBranchesName();
        for (String branchName : branchesNames) {
            MenuItem menuItem = new MenuItem();
            menuItem.textProperty().setValue(branchName);
            menuItem.setOnAction(event -> {
                try {
                    onBranchButtonMenuItemClick(menuItem.getText());
                    branchNameProperty.setValue(menuItem.getText());
                } catch (ParseException | RepositoryNotFoundException | InvalidNameException | BranchNotFoundException e) {
                    e.printStackTrace();
                }
            });
            currentBranchMenuButton.getItems().add(menuItem);
        }
    }

    void onBranchButtonMenuItemClick(String branchName) throws ParseException, RepositoryNotFoundException,
            InvalidNameException, BranchNotFoundException{
        String headMessage = "There are unsaved changes";
        String bodyMessage = "are you sure you want to switch branch?";
        try {
            engine.pickHeadBranch(branchName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UncommitedChangesException e) {
            BasicPopupScreenController controller = event1 -> {
                try {
                    engine.forcedChangeBranch(branchName);
                } catch (ParseException | IOException | PreviousCommitsLimitExceededException ignored) {}
            };
            try {
                createNotificationPopup(controller, true, headMessage, bodyMessage, "Cancel");
            } catch(IOException ignored) {}
        } catch (PreviousCommitsLimitExceededException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openUserNameChangeScreen(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/UserNameScreen.fxml"));
        Parent layout = loader.load();
        UserNameScreenController userNameScreenController = loader.getController();
        userNameScreenController.setUserNameProperty(userNameProperty);
        createPopup(layout, userNameScreenController);
    }

    @FXML
    void openNewRepositoyScreenAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/createNewRepositoryScreen.fxml"));
        Parent layout = loader.load();
        CreateNewRepositoryScreenController createNewRepositoryScreenController = loader.getController();
        createNewRepositoryScreenController.setRepositoryNameProperty(repositoryNameProperty);
        createNewRepositoryScreenController.bindings();
        createPopup(layout, createNewRepositoryScreenController);
    }

    void createPopup(Parent layout, BasicController basicController) {
        Scene scene = new Scene(layout);
        Stage currStage = new Stage();
        basicController.setStage(stage);
        currStage.setScene(scene);
        currStage.initModality(Modality.WINDOW_MODAL);
        currStage.initStyle(StageStyle.UNDECORATED);
        currStage.initOwner(stage);
        currStage.setMinHeight(300);
        currStage.setMinWidth(300);
        basicController.setEngine(engine);
        ResizeHelper.addResizeListener(currStage);
        ResizeHelper.setMovable(true);
        currStage.showAndWait();
        ResizeHelper.setMovable(false);
    }

    private void createNotificationPopup(BasicPopupScreenController controllerInterface,
                                         boolean hasAcceptButton,
                                         String headMessage, String bodyMessage, String cancelButtonText) throws IOException {
        if(controllerInterface == null){
            controllerInterface = event -> { };
        }
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/popupScreen.fxml"));
        Parent layout = loader.load();

        PopupScreenController popupScreenController = loader.getController();
        popupScreenController.setController(controllerInterface);
        popupScreenController.setPopupAcceptButtonVisibility(hasAcceptButton);
        popupScreenController.setPopupCancelButtonText(cancelButtonText);
        popupScreenController.setPopupHeadMessageLableText(headMessage);
        popupScreenController.setPopupBodyMessageLabel(bodyMessage);
        createPopup(layout, popupScreenController);
    }
    @FXML
    void openRepositoryFromXmlAction(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Xml files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(stage);
        try{
            engine.loadRepositoryFromXML(file.getAbsolutePath(), false);
        } catch (IllegalPathException | ParseException | XmlFileException | PreviousCommitsLimitExceededException | JAXBException | IOException e) {
            createNotificationPopup(null, false, "Repository from XML notification", e.getMessage(),"Close");
        } catch (RepositoryAlreadyExistsException e) {
            BasicPopupScreenController basicPopupScreenController1 = event1 -> {
                try {
                    engine.loadRepositoryFromXML(file.getAbsolutePath(),true);
                    Button chosen = (Button) event1.getSource();
                    Stage curStage = (Stage) chosen.getScene().getWindow();
                    curStage.close();
                    createNotificationPopup(null, false, "Repository from XML notification", "Repository created successfully.","Close");
                } catch (JAXBException | IOException | ParseException | PreviousCommitsLimitExceededException | XmlFileException | IllegalPathException | RepositoryAlreadyExistsException ex) {
                    try {
                        createNotificationPopup(event11 -> {
                            Button chosen = (Button) event11.getSource();
                            Stage curStage = (Stage) chosen.getScene().getWindow();
                            curStage.close();
                        },false,"Repository from XML error",e.getMessage(),"Close");
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }
                }
            };
            createNotificationPopup(basicPopupScreenController1, true,"Repository already exists notification","Would you like to replace current repository with XML repository?","Cancel");
        }
        repositoryNameProperty.setValue(engine.getRepositoryName());
    }

    @FXML
    void openRepositoryFromFolderChooserAction(ActionEvent event) throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory =
                directoryChooser.showDialog(stage);
        try {
            engine.switchRepository(selectedDirectory.getAbsolutePath());
            repositoryNameProperty.setValue(engine.getRepositoryName());
        } catch (IOException | ParseException | RepositoryNotFoundException e) {
            createNotificationPopup(null,false,"Repository creation notification",e.getMessage(),"Close");
        }
        loadBranchesToUserInterface();
    }

    @FXML
    void onOpenChangesRefreshButtonClicked(MouseEvent event) {
        Integer editedCount = 0, deletedCount = 0, newCount = 0;
        editedFilesVbox.getChildren().clear();
        deletedFilesVbox.getChildren().clear();
        newFilesVbox.getChildren().clear();
        try {
            Map<FileStatus, SortedSet<Delta.DeltaFileItem>> openChanges =  engine.getWorkingCopyStatusMap();
            for(Map.Entry<FileStatus, SortedSet<Delta.DeltaFileItem>> entry : openChanges.entrySet()){
                for(Delta.DeltaFileItem item : entry.getValue()) {
                    if (entry.getKey().equals(FileStatus.EDITED)) {
                        editedFilesVbox.getChildren().add(new Label(item.getFullPath()));
                        editedCount++;
                    }
                    if (entry.getKey().equals(FileStatus.REMOVED)) {
                        deletedFilesVbox.getChildren().add(new Label(item.getFullPath()));
                        deletedCount++;
                    }
                    if(entry.getKey().equals(FileStatus.NEW)) {
                        newFilesVbox.getChildren().add(new Label(item.getFullPath()));
                        newCount++;
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
            e.printStackTrace();
        } catch (PreviousCommitsLimitExceededException e) {
            e.printStackTrace();
        }
    }
}
