package com.magit.gui;

import com.magit.controllers.PopupScreenController;
import com.magit.controllers.interfaces.BasicController;
import com.magit.controllers.interfaces.BasicPopupScreenController;
import com.magit.logic.system.MagitEngine;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.*;

import java.io.IOException;

public class PopupScreen {
    private Stage stage;
    private MagitEngine engine;

    public PopupScreen(Stage stage, MagitEngine engine) {
        this.stage = stage;
        this.engine = engine;
    }

    public void createPopup(Parent layout, BasicController basicController) {
        Scene scene = new Scene(layout);
        Stage currStage = new Stage();
        basicController.setStage(stage);
        currStage.setScene(scene);
        currStage.initModality(Modality.WINDOW_MODAL);
        currStage.initStyle(StageStyle.UNDECORATED);
        currStage.initOwner(stage);
        //currStage.setMinHeight(300);
        //currStage.setMinWidth(300);
        currStage.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                currStage.setX(stage.getX() + stage.getWidth()/2d - currStage.getWidth()/2d);
                currStage.setY(stage.getY() + stage.getHeight()/2d - currStage.getHeight()/2d);
            }
        });
        currStage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode() == KeyCode.ESCAPE) {
                currStage.close();
            }
        });
        basicController.setEngine(engine);
        ResizeHelper.addResizeListener(currStage);
        ResizeHelper.setMovable(true);
        currStage.showAndWait();
        ResizeHelper.setMovable(false);
    }

     public void createNotificationPopup(BasicPopupScreenController controllerInterface,
                                         boolean hasAcceptButton,
                                         String headMessage, String bodyMessage, String cancelButtonText) throws IOException {
        if(controllerInterface == null){
            controllerInterface = event -> { };
        }
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/magit/resources/fxml/popupScreen.fxml"));
        Parent layout = loader.load();

        PopupScreenController popupScreenController = loader.getController();
        popupScreenController.setController(controllerInterface);
        popupScreenController.setPopupAcceptButtonVisibility(hasAcceptButton);
        popupScreenController.setPopupCancelButtonText(cancelButtonText);
        popupScreenController.setPopupHeadMessageLableText(headMessage);
        popupScreenController.setPopupBodyMessageLabel(bodyMessage);
        createPopup(layout, popupScreenController);
    }
}
