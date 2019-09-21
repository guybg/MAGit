package com.magit.gui;

import com.magit.controllers.MainScreenController;
import com.magit.logic.system.MagitEngine;
import com.magit.webLogic.users.UserManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;

public class MagitGui extends Application {
    private MagitEngine engine;
    @Override
    public void start(Stage primaryStage) throws Exception {
        engine = new MagitEngine();
        primaryStage.setTitle("Magit Desktop");
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.getIcons().add(new Image("/com/magit/resources/images/appIcon.png"));
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("/com/magit/resources/fxml/homeScreen.fxml");
        fxmlLoader.setLocation(url);
        Parent root = fxmlLoader.load(url.openStream());
        root.setEffect(new DropShadow(2d, 0d, +2d, Color.BLACK));
        MainScreenController controller = fxmlLoader.getController();
        controller.setEngine(engine);
        controller.setStage(primaryStage);
        Scene scene = new Scene(root);
        scene.setFill(null);
        primaryStage.setMinWidth(970);
        primaryStage.setMinHeight(680);

        final String cssURL = this.getClass().getResource("/com/magit/resources/css/home.css").toExternalForm();
        scene.getStylesheets().addAll(cssURL);
        primaryStage.setScene(scene);
        primaryStage.show();
        ResizeHelper.addResizeListener(primaryStage);
    }
}
