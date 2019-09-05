package com.magit.controllers;


import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import com.fxgraph.graph.PannableCanvas;
import com.magit.controllers.interfaces.BasicController;
import com.magit.controllers.interfaces.BasicPopupScreenController;
import com.magit.gui.PopupScreen;
import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.managers.MergeEngine;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.FileItem;
import com.magit.logic.system.objects.FileItemInfo;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.visual.layout.CommitTreeLayout;
import com.magit.logic.visual.node.CommitNode;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.control.CheckBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.*;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;
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
        updateBindings();
        updateListeners();
        showWelcomeNode();
    }

    @FXML
    private MenuItem branchesMenuItem;
    @FXML
    private ListView<HBox> branchesListView;
    @FXML
    private Label menuButtonBranchNameLabel;
    @FXML
    private Label menuButtonRepositoryNameLabel;
    //@FXML private AnchorPane anchorPane;
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
    private MenuItem fetchMenuItem;
    @FXML
    private MenuItem cloneMenuItem;
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
    private ListView<Label> editedFilesListView;
    @FXML
    private TitledPane deletedTitlePane;
    @FXML
    private ListView<Label> deletedFilesListView;
    @FXML
    private TitledPane newFilesTitlePane;
    @FXML
    private ListView<Label> newFilesListView;
    @FXML
    private Button openChangesRefreshButton;
    @FXML
    private CheckBox checkBox;
    @FXML
    private AnchorPane middleAnchorPane;
    @FXML
    private SplitPane middleHSplitPane;
    @FXML
    private Label moveScreenLabel;
    @FXML
    private AnchorPane progressBarPane;
    @FXML
    private MenuItem branchesHistoryMenuItem;
    @FXML
    private Label DarkThemeLabel;
    @FXML
    private Label BrightThemeLabel;
    @FXML
    private Label PinkThemeLabel;
    @FXML
    private MenuItem DarkThemeMenuItem;
    @FXML
    private MenuItem BrightThemeMenuItem;
    @FXML
    private MenuItem PinkThemeMenuItem;
    @FXML
    private Button windowSizeButton;
    @FXML
    private ImageView sizeImageView;
    @FXML
    private GridPane bodyGrid;
    @FXML
    private AnchorPane pane;

    @FXML
    void OnBrightThemeClicked(ActionEvent event) {
        final String brightThemeUrl = this.getClass().getResource("/com/magit/resources/css/home.css").toExternalForm();
        (stage.getScene()).getStylesheets().clear();
        (stage.getScene()).getStylesheets().add(brightThemeUrl);
    }

    @FXML
    void OnDarkThemeClicked(ActionEvent event) {
        final String darkThemeUrl = this.getClass().getResource("/com/magit/resources/css/dark.css").toExternalForm();
        (stage.getScene()).getStylesheets().clear();
        (stage.getScene()).getStylesheets().add(darkThemeUrl);
    }

    @FXML
    void OnPinkThemeClicked(ActionEvent event) {
        final String pinkThemeUrl = this.getClass().getResource("/com/magit/resources/css/pink.css").toExternalForm();
        (stage.getScene()).getStylesheets().clear();
        (stage.getScene()).getStylesheets().add(pinkThemeUrl);
    }

    private ObservableList<FileItemInfo> fileItemInfos;

    @FXML
    void onExitApplication(ActionEvent event) {
        stage.close();
    }


    @FXML
    void OnCloseButtonAction(ActionEvent event) {
        Button closeButton = (Button) event.getSource();
        Stage stage = (Stage) closeButton.getScene().getWindow();

        stage.close();
    }

    @FXML
    void OnMinimizeButtonAction(ActionEvent event) {
        Button minimizeButton = (Button) event.getSource();
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    void OnChangeWindowSizeButtonAction(ActionEvent event) {
        Button sizeButton = (Button) event.getSource();
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        if (stage.isMaximized()) {
            restoreWindow();
        } else {
            maximize();
        }
    }

    private double StageWidthBeforeMaximize;

    private void maximize() {
        StageWidthBeforeMaximize = stage.getWidth();
        stage.setMaximized(true);
        setStageToMaximizedAccordingToCurrentScreen();
        stage.iconifiedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && stage.isMaximized()) {
                setStageToMaximizedAccordingToCurrentScreen();
            }
        });
        sizeImageView.setId("window-restore-image");
    }

    void setStageToMaximizedAccordingToCurrentScreen() {
        // Get current screen of the stage
        ObservableList<Screen> screens = Screen.getScreensForRectangle(new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));

        // Change stage properties
        Rectangle2D bounds = screens.get(0).getVisualBounds();
        stage.setX(bounds.getMinX() - 4);
        stage.setY(bounds.getMinY() - 4);
        stage.setWidth(bounds.getWidth() + 8);
        stage.setHeight(bounds.getHeight() + 8);
    }


    private void restoreWindow() {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        stage.setMaximized(false);
        if (stage.getScene().getWindow().getY() < 0) {
            stage.getScene().getWindow().setY(0);
        }
        //stage.centerOnScreen();
        sizeImageView.setId("window-maximize-image");
    }

    @FXML
    void OnMouseDragged(MouseEvent event) {
        if (stage.isMaximized()) {
            restoreWindow();
        } else {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }

    @FXML
    void OnMousePressed(MouseEvent event) {
        if (!stage.isMaximized()) {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        } else {
            xOffset = event.getSceneX() / (stage.getWidth() / StageWidthBeforeMaximize);
        }
    }

    @FXML
    void onMerge(ActionEvent event) {
        PopupScreen popupScreen = new PopupScreen(stage, engine);
        try {
            engine.activeBranchHasUnhandledMerge();
            popupScreen.createMergeScreen();
        } catch (UnhandledMergeException e) {
            popupScreen.showErrorMessage(e.getMessage());
            popupScreen.createMergeScreenWithPreChosenBranch();
        }

    }

    @FXML
    void onShowBranchesHistory(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("/com/magit/resources/fxml/branchesHistoryScreen.fxml");
        fxmlLoader.setLocation(url);
        SplitPane root = null;
        try {
            root = fxmlLoader.load(url.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage pStage = new Stage();
        pStage.initModality(Modality.APPLICATION_MODAL);
        Graph graph = new Graph();
        Model model = graph.getModel();
        final Scene scene = new Scene(root, 700, 400);

        ((BranchesHistoryScreenController) fxmlLoader.getController()).setEngine(engine);
        ((BranchesHistoryScreenController) fxmlLoader.getController()).setStage(stage);
        //TreeSet<CommitNode> nodes = null;
        engine.guiBranchesHistory(nodes -> {
            graph.beginUpdate();
            for (ICell node : nodes) {
                if (!model.getAllCells().contains(node))
                    model.addCell(node);
            }
            graph.endUpdate();
            graph.layout(new CommitTreeLayout());
            pStage.setMinWidth(936);
            pStage.setMinHeight(534);
            pStage.setScene(scene);
            pStage.show();
            Platform.runLater(() -> {
                ScrollPane scrollPane = (ScrollPane) scene.lookup("#scrollpaneContainer");
                PannableCanvas canvas = graph.getCanvas();
                scrollPane.setContent(canvas);
                graph.getUseViewportGestures().set(false);
                graph.getUseNodeGestures().set(false);
            });
        }, s -> {
            PopupScreen popupScreen = new PopupScreen(stage, engine);
            try {
                popupScreen.createNotificationPopup(null, false, "Oops, cannot show history", s, "Close");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, model, fxmlLoader.getController());
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, commitMessageTextArea.getText());
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
        if (repositoryNameProperty.getValue().equals("")) return;
        branchesListView.getItems().clear();
        branchNameProperty.setValue(engine.getHeadBranchName());
        branchesListView.setFocusTraversable(false);
        Collection<Branch> branches = engine.getBranches();
        for (Branch branch : branches) {
            HBox branchHbox = new HBox();
            Label labelOfBranch = new Label(branch.getBranchName());
            Button deleteBranchButton = new Button();
            deleteBranchButton.setText("Delete");
            branchHbox.setAlignment(Pos.CENTER);

            branchHbox.getChildren().add(labelOfBranch);
            branchHbox.getChildren().add(deleteBranchButton);
            branchHbox.setSpacing(5);
            if (branch.getIsRemote()) {
                branchHbox.setId("remote-branch-cell");
            }
            HBox.setHgrow(labelOfBranch, Priority.SOMETIMES);
            HBox.setHgrow(deleteBranchButton, Priority.SOMETIMES);
            deleteBranchButton.setWrapText(true);
            deleteBranchButton.setMinWidth(70);
            labelOfBranch.setMaxWidth(Double.MAX_VALUE);
            branchHbox.prefWidthProperty().bind(branchesListView.widthProperty().subtract(20));
            labelOfBranch.setAlignment(Pos.BASELINE_LEFT);

            deleteBranchButton.setOnAction(event -> deleteBranch(((Label) ((HBox) ((Button) event.getSource()).getParent()).getChildren().get(0)).getText()));

            Tooltip branchInfo = new Tooltip();
            try {
                branchInfo.textProperty().setValue(engine.guiGetBranchInfo(branch));
                branchInfo.setHeight(20);
                branchInfo.setWidth(branchesListView.getWidth());
                branchInfo.setFont(new Font(20));
            } catch (ParseException | PreviousCommitsLimitExceededException | IOException ignored) {
            }
            labelOfBranch.setTooltip(branchInfo);
            branchesListView.getItems().add(branchHbox);
            if (branch.getIsRemote()) deleteBranchButton.setVisible(false);
        }
        branchesListView.setOnMouseClicked(event -> {
            try {
                if (branchesListView.getSelectionModel().getSelectedItem() == null)
                    return;
                onBranchButtonMenuItemClick(((Label) branchesListView.getSelectionModel().getSelectedItem().getChildren().get(0)).getText());
            } catch (ParseException | BranchNotFoundException | InvalidNameException | RepositoryNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    void deleteBranch(String branchName) {
        PopupScreen popupScreen = new PopupScreen(stage, engine);
        try {
            popupScreen.createNotificationPopup(event -> {
                try {
                    engine.deleteBranch(branchName);
                } catch (IOException | RepositoryNotFoundException | BranchNotFoundException e) {
                    e.printStackTrace();
                } catch (ActiveBranchDeletedException | RemoteBranchException e) {
                    try {
                        popupScreen.createNotificationPopup(event1 -> ((Stage) ((Button) event1.getSource()).getScene().getWindow()).close(), false, "Oops.. something went wrong", e.getMessage(), "Close");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
            }, true, "Are you sure?", "Deleting branch cannot be reverted", "Cancel");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onPull(ActionEvent event) throws IOException {
        PopupScreen popupScreen = new PopupScreen(stage, engine);
        try {
            engine.pull();
            popupScreen.createMergeScreenWithPreChosenBranch();
        } catch (ParseException | CommitNotFoundException | RemoteReferenceException | PreviousCommitsLimitExceededException | IOException e) {
            try {
                popupScreen.createNotificationPopup(null, false, "Oops.. something went wrong", e.getMessage(), "Close");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (MergeNotNeededException e) {
            try {
                popupScreen.createNotificationPopup(null, false, "Fast forward merge notification", "Local repository is up-to-date.", "Close");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (UnhandledMergeException e) {
            try {
                popupScreen.createNotificationPopup(null, false, "Unhandled merge notification", "Please solve existing unhandled merge by clicking on Branch->merge and try this operation again.", "Close");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (FastForwardException e) {
            try {
                popupScreen.createNotificationPopup(null, false, "Fast forward merge notification", e.getMessage(), "Close");
                popupScreen.createMergeScreenWithPreChosenBranch();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (UncommitedChangesException e) {
            popupScreen.createNotificationPopup(null, false, "Oops.. there are open changes", e.getMessage() + ", please commit them before pulling.", "Close");
        } catch (RepositoryNotFoundException e) {
            e.printStackTrace();
        } catch (RemoteBranchException | MergeException e) {
            popupScreen.createNotificationPopup(null, false, "Oops.. something went wrong", e.getMessage(), "Close");
            ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
        }
    }

    @FXML
    void onPush(ActionEvent event) {
        PopupScreen popupScreen = new PopupScreen(stage, engine);
        try {
            engine.push();
            try {
                popupScreen.createNotificationPopup(null, false, "Push notification", "Files pushed successfully!", "Close");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            try {
                popupScreen.createNotificationPopup(null, false, "Oops.. something went wrong", e.getMessage(), "Close");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (UnhandledMergeException | RemoteReferenceException | PushException | RemoteBranchException | CommitNotFoundException | ParseException | UncommitedChangesException | PreviousCommitsLimitExceededException e) {
            try {
                popupScreen.createNotificationPopup(null, false, "Oops.. cannot push", e.getMessage(), "Close");
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }

    @FXML
    void onClone(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/fxml/cloneScreen.fxml"));
        Parent layout = loader.load();
        CloneScreenController createNewRepositoryScreenController = loader.getController();
        PopupScreen popupScreen = new PopupScreen(stage, engine);
        popupScreen.createPopup(layout, createNewRepositoryScreenController);
    }

    @FXML
    void onFetch(ActionEvent event) {
        try {
            engine.fetch();
        } catch (PreviousCommitsLimitExceededException | CommitNotFoundException | ParseException | IOException | RemoteReferenceException e) {
            PopupScreen popupScreen = new PopupScreen(stage, engine);
            try {
                popupScreen.createNotificationPopup(null, false, "Oops could not fetch", e.getMessage(), "Close");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IllegalPathException e) {
            e.printStackTrace();
        }
    }


    @FXML
    void onResetBranchMenuItemClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/fxml/resetBranchScreen.fxml"));
        Parent layout = loader.load();
        ResetBranchScreenController resetBranchScreenController = loader.getController();
        PopupScreen popupScreen = new PopupScreen(stage, engine);
        popupScreen.createPopup(layout, resetBranchScreenController);
        updateDifferences();
    }

    private void onBranchButtonMenuItemClick(String branchName) throws ParseException, RepositoryNotFoundException,
            InvalidNameException, BranchNotFoundException {
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
                } catch (ParseException | IOException | PreviousCommitsLimitExceededException ignored) {
                }
                Button button = (Button) event1.getSource();
                ((Stage) (button.getScene().getWindow())).close();
            };
            try {
                PopupScreen popupScreen = new PopupScreen(stage, engine);
                popupScreen.createNotificationPopup(controller, true, headMessage, e.getMessage(), "Cancel");
            } catch (IOException ignored) {
            }
        } catch (RemoteBranchException e) {
            PopupScreen popupScreen = new PopupScreen(stage, engine);
            try {
                popupScreen.createNotificationPopup(event -> {
                    PopupScreen popupScreen1 = new PopupScreen(((Stage) ((Button) event.getSource()).getScene().getWindow()), engine);
                    try {
                        engine.createRemoteTrackingBranch(e.getBranchName());
                        popupScreen1.createNotificationPopup(null, false, "Remote tracking branch creation notification", "Remote tracking branch created successfully!", "Cancel");
                        ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
                    } catch (BranchNotFoundException | RepositoryNotFoundException | InvalidNameException | RemoteReferenceException | IOException | BranchAlreadyExistsException ex) {
                        try {
                            popupScreen1.createNotificationPopup(null, false, "Oops.. cannot create remote tracking branch", ex.getMessage(), "Cancel");
                        } catch (IOException exc) {
                            exc.printStackTrace();
                        } finally {
                            ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
                        }
                    }
                }, true, "Oops, cannot switch to chosen branch", e.getMessage(), "Cancel");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (PreviousCommitsLimitExceededException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openUserNameChangeScreen(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/fxml/generalScreenEnterString.fxml"));
        Parent layout = loader.load();
        GeneralScreenEnterStringController userNameController =
                getGeneralScreen(loader, "Switch User Name", "User Name:");
        userNameController.setController(buttonEvent -> {
            try {
                String fieldValue = userNameController.getTextFieldValue();
                engine.updateUserName(fieldValue);
                userNameProperty.setValue(fieldValue);
                ((Stage) ((Button) buttonEvent.getSource()).getScene().getWindow()).close();
            } catch (InvalidNameException e) {
                userNameController.setError(e.getMessage());
            }
        });
        PopupScreen popupScreen = new PopupScreen(stage, engine);
        popupScreen.createPopup(layout, userNameController);
    }

    @FXML
    void openNewRepositoryScreenAction() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/fxml/createNewRepositoryScreen.fxml"));
        Parent layout = loader.load();
        CreateNewRepositoryScreenController createNewRepositoryScreenController = loader.getController();
        createNewRepositoryScreenController.setRepositoryNameProperty(repositoryNameProperty);
        createNewRepositoryScreenController.setRepositoryPathProperty(repositoryPathProperty);
        createNewRepositoryScreenController.bindings();
        PopupScreen popupScreen = new PopupScreen(stage, engine);
        popupScreen.createPopup(layout, createNewRepositoryScreenController);
        //events on properties handles branches load, diff loads
    }

    void showWelcomeNode() {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/fxml/welcomeNode.fxml"));
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

        middleAnchorPane.getChildren().add(welcomeNode);
        AnchorPane.setBottomAnchor(welcomeNode, 1.0);
        AnchorPane.setLeftAnchor(welcomeNode, 1.0);
        AnchorPane.setRightAnchor(welcomeNode, 1.0);
        AnchorPane.setTopAnchor(welcomeNode, 1.0);
    }

    @FXML
    void onGetCommitHistoryClicked(ActionEvent event) {
        engine.guiCollectCommitHistoryInfo(new Consumer<ObservableList<FileItemInfo>>() {
            @Override
            public void accept(ObservableList<FileItemInfo> fileItemInfos) {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/com/magit/resources/fxml/tableScreen.fxml"));
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
        loader.setLocation(getClass().getResource("/com/magit/resources/fxml/importXmlNode.fxml"));
        Node table = null;
        try {
            table = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImportXmlNodeController controller = loader.getController();
        controller.setEngine(engine);
        controller.setStage(stage);
        controller.setRepositoryNameProperty(repositoryNameProperty);
        controller.setRepositoryPathProperty(repositoryPathProperty);
        AnchorPane.setBottomAnchor(table, 0.0);
        AnchorPane.setLeftAnchor(table, 0.0);
        AnchorPane.setRightAnchor(table, 0.0);
        AnchorPane.setTopAnchor(table, 0.0);
        progressBarPane.getChildren().add(table);
        progressBarPane.toFront();
        controller.start(false, () -> progressBarPane.toBack());

    }

    @FXML
    void openRepositoryFromFolderChooserAction() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory =
                directoryChooser.showDialog(stage);
        if (selectedDirectory == null)
            return;
        try {
            engine.switchRepository(selectedDirectory.getAbsolutePath());
            repositoryNameProperty.setValue(engine.getRepositoryName());
            repositoryPathProperty.setValue(engine.guiGetRepositoryPath());
            //events on properties handles branches load, diff loads //loadBranchesToUserInterface();
        } catch (IOException | ParseException | RepositoryNotFoundException e) {
            PopupScreen popupScreen = new PopupScreen(stage, engine);
            popupScreen.createNotificationPopup(null, false, "Repository creation notification", e.getMessage(), "Close");
        }
    }

    @FXML
    void onOpenChangesRefreshButtonClicked(MouseEvent event) {
        updateDifferences();
    }

    void updateDifferences() {
        Integer editedCount = 0, deletedCount = 0, newCount = 0;
        editedFilesListView.getItems().clear();
        deletedFilesListView.getItems().clear();
        newFilesListView.getItems().clear();
        try {
            Map<FileStatus, SortedSet<Delta.DeltaFileItem>> openChanges = engine.getWorkingCopyStatusMap();
            for (Map.Entry<FileStatus, SortedSet<Delta.DeltaFileItem>> entry : openChanges.entrySet()) {
                for (Delta.DeltaFileItem item : entry.getValue()) {
                    switch (entry.getKey()) {
                        case EDITED:
                            createDiffLabels(item, editedFilesListView);
                            editedCount++;
                            break;
                        case REMOVED:
                            createDiffLabels(item, deletedFilesListView);
                            deletedCount++;
                            break;
                        case NEW:
                            createDiffLabels(item, newFilesListView);
                            newCount++;
                            break;
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
            BasicPopupScreenController controller = event1 -> {
            };
            try {
                PopupScreen popupScreen = new PopupScreen(stage, engine);
                popupScreen.createNotificationPopup(controller, false, "Refresh notification", e.getMessage(), "Close");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (PreviousCommitsLimitExceededException e) {
            e.printStackTrace();
        }
    }


    private void createDiffLabels(Delta.DeltaFileItem item, ListView<Label> editedFilesListView) {
        Label itemLocation = new Label(item.getFullPath());
        String lastModifier = item.getLastUpdater();
        String commitDate = item.getLastModified();
        if (item.getLastUpdater().equals("")) {
            lastModifier = userNameProperty.getValue();
            commitDate = "not committed";
        }
        itemLocation.setTooltip(new Tooltip(String.format("Location: %s%sFile name: %s%sLast modifier: %s%sCommit date: %s", item.getFullPath(), System.lineSeparator(), item.getFileName(), System.lineSeparator(), lastModifier, System.lineSeparator(), commitDate)));
        editedFilesListView.getItems().add(itemLocation);
    }

    private void createDiffLabels(FileItemInfo item, ListView<Label> editedFilesListView) {
        Label itemLocation = new Label(item.getFileLocation());
        String lastModifier = item.getFileLastUpdater();
        String commitDate = item.getFileLastModified();
        if (item.getFileLastUpdater().equals("")) {
            lastModifier = userNameProperty.getValue();
            commitDate = "not committed";
        }
        itemLocation.setTooltip(new Tooltip(String.format("Location: %s%sFile name: %s%sLast modifier: %s%sCommit date: %s", item.getFileLocation(), System.lineSeparator(), item.getFileName(), System.lineSeparator(), lastModifier, System.lineSeparator(), commitDate)));
        editedFilesListView.getItems().add(itemLocation);
    }

    @FXML
    private void onDeleteBranchClick() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/fxml/generalScreenEnterString.fxml"));
        Parent layout = loader.load();
        GeneralScreenEnterStringController deleteBranchCotnroller =
                getGeneralScreen(loader, "Create New Branch", "Branch Name:");
        deleteBranchCotnroller.setController(event -> {
            String branchName = deleteBranchCotnroller.getTextFieldValue();
            try {
                try {
                    engine.deleteBranch(branchName);
                    ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
                } catch (IOException e) {
                    deleteBranchCotnroller.setError("Please enter valid name.");
                } catch (RemoteBranchException e) {
                    deleteBranchCotnroller.setError(e.getMessage());
                }
            } catch (ActiveBranchDeletedException ex) {
                deleteBranchCotnroller.setError("Can't delete active branch");
            } catch (BranchNotFoundException ex) {
                deleteBranchCotnroller.setError("Branch doesn't exist, or branch name is written wrong.");
            } catch (RepositoryNotFoundException ex) {
                deleteBranchCotnroller.setError("No repository loaded.");
            }
        });
        PopupScreen popupScreen = new PopupScreen(stage, engine);
        popupScreen.createPopup(layout, deleteBranchCotnroller);
    }

    @FXML
    void onNewBranchClicked() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/fxml/generalScreenEnterString.fxml"));
        Parent layout = loader.load();
        GeneralScreenEnterStringController newBranchController =
                getGeneralScreen(loader, "Create new branch", "Branch name:");
        newBranchController.setCheckBoxVisible();
        PopupScreen popupScreen = new PopupScreen(stage, engine);
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
                            ((Stage) ((Button) cEvent.getSource()).getScene().getWindow()).close();
                        }, true, "Are you sure?", "There are unsaved changes, switching branch may cause lose of data.", "Cancel");
                    } catch (RemoteBranchException e) {
                        //needed ?? todo
                    }
                }
            } catch (IOException | InvalidNameException | RepositoryNotFoundException | BranchAlreadyExistsException e) {
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

    public static GeneralScreenEnterStringController getGeneralScreen(FXMLLoader loader, String headLabelValue, String keyLabelValue)
            throws IOException {
        GeneralScreenEnterStringController generalController = loader.getController();
        generalController.setHeadLabel(headLabelValue);
        generalController.setKeyLabel(keyLabelValue);

        return generalController;
    }

    private void setLastCommitLabels() {
        try {
            ArrayList<String> commitInfo = engine.getLastCommitDateAndMessage();
            if (commitInfo != null) {
                commitDateLeftDownLabel.setText(commitInfo.get(0));
                commitMessageLeftDownLabel.setText(commitInfo.get(1));
            } else {
                commitDateLeftDownLabel.setText("");
                commitMessageLeftDownLabel.setText("");
            }
        } catch (IOException | PreviousCommitsLimitExceededException | ParseException e) {
            e.printStackTrace();
        }

    }

    private void updatePushAndPullButtons() {
        try {
            if(engine.repositoryHasRemoteReference()){
                pushMenuItem.setDisable(false);
            }
            if (engine.activeBranchIsTrackingAfter()) {
                pullMenuItem.setDisable(false);
            } else {
                //pushMenuItem.setDisable(true);
                pullMenuItem.setDisable(true);
            }
        } catch (RepositoryNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateFetchButton() {
        try {
            if (engine.repositoryHasRemoteReference()) {
                fetchMenuItem.setDisable(false);
            } else {
                fetchMenuItem.setDisable(true);
            }
        } catch (RepositoryNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateOnRepositoryNameChangedButtons() {
        commitToLeftDownButton.setDisable(false);
        resetBranchMenuItem.setDisable(false);
        deleteBranchMenuItem.setDisable(false);
        newBranchMenuItem.setDisable(false);
        commitHistoryMenuItem.setDisable(false);
        branchesMenuItem.setVisible(true);
        branchesHistoryMenuItem.setDisable(false);
        mergeMenuItem.setDisable(false);
    }

    private void updateOnScreenInitButtons() {
        commitToLeftDownButton.setDisable(true);
        resetBranchMenuItem.setDisable(true);
        deleteBranchMenuItem.setDisable(true);
        newBranchMenuItem.setDisable(true);
        commitHistoryMenuItem.setDisable(true);
        branchesMenuItem.setVisible(false);
        branchesHistoryMenuItem.setDisable(true);
        mergeMenuItem.setDisable(true);
        pushMenuItem.setDisable(true);
        pullMenuItem.setDisable(true);
        fetchMenuItem.setDisable(true);
    }

    private void updateBindings() {
        menuItem1Label.prefWidthProperty().bind(currentRepositoryMenuButton.widthProperty().subtract(15));
        switchUserLabel.prefWidthProperty().bind(userNameMenuButton.widthProperty().subtract(15));
        branchesListView.prefWidthProperty().bind(currentBranchMenuButton.widthProperty().subtract(15));
        branchesListView.setMaxWidth(Control.USE_PREF_SIZE);

        userNameProperty = new SimpleStringProperty();
        repositoryPathProperty = new SimpleStringProperty();
        userNameProperty.setValue("Administrator");
        userNameMenuButton.textProperty().bind(userNameProperty);
        if (repositoryNameProperty == null) {
            repositoryNameProperty = new SimpleStringProperty();
            repositoryNameProperty.setValue("");
        }

        menuButtonRepositoryNameLabel.textProperty().bind(Bindings
                .when(repositoryNameProperty.isNotEqualTo(""))
                .then(repositoryNameProperty)
                .otherwise("No repository"));
        branchNameProperty = new SimpleStringProperty();
        menuButtonBranchNameLabel.textProperty().bind(Bindings
                .when(branchNameProperty.isNotEqualTo(""))
                .then(branchNameProperty)
                .otherwise("No branch"));
        commitToLeftDownButton.textProperty().bind(Bindings.format("%s %s", "Commit to", branchNameProperty));
    }

    private void updateListeners() {
        branchNameProperty.addListener((observable, oldValue, newValue) -> {
            updateDifferences();
            updatePushAndPullButtons();
            setLastCommitLabels();
        });
        updateOnScreenInitButtons();
        repositoryNameProperty.addListener((observable, oldValue, newValue) -> {
            updateOnRepositoryNameChangedButtons();
        });
        repositoryPathProperty.addListener((observable, oldValue, newValue) -> {
            currentRepositoryMenuButton.tooltipProperty().setValue(new Tooltip(repositoryPathProperty.getValue()));
            loadBranchesToUserInterface();
            updateFetchButton();
            updatePushAndPullButtons();
        });
        moveScreenLabel.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                if (stage.isMaximized()) restoreWindow();
                else maximize();
            }
        });
    }
}
