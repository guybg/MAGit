package com.magit.gui;

import com.magit.logic.exceptions.InvalidNameException;
import com.magit.logic.system.MagitEngine;
import com.magit.properties.UserName;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class UserNameScreenController implements BasicController, Initializable {

    private Stage stage = null;
    private MagitEngine engine;
    private StringProperty userNameProperty;



    public void setEngine(MagitEngine engine) {
        this.engine = engine;
    }

    public void setUserNameProperty(StringProperty userNameProperty) {
        this.userNameProperty = userNameProperty;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
       // pane.prefWidthProperty().bind(Bindings.length(userNameErrorMessageLabel.textProperty()));
    }


    @FXML
    private AnchorPane pane;

    @FXML
    private TextField userNameTextFiled;

    @FXML
    private Button buttonUserNameOk;

    @FXML
    private Button buttonUserNameCancel;

    @FXML
    private Label userNameErrorMessageLabel;


    @FXML
    private VBox containerVbox;


    @FXML
    void cancelUserName(MouseEvent event) {
        Button closeButton = (Button)event.getSource();
        Stage curStage = (Stage) closeButton.getScene().getWindow();
        curStage.close();
    }

    @FXML
    void setUserName(MouseEvent event)  {
        try {
            engine.updateUserName(userNameTextFiled.getText());
        } catch (InvalidNameException e) {
            userNameErrorMessageLabel.setText(e.getMessage());
            return;
        }
        userNameProperty.setValue(userNameTextFiled.getText());
        Button closeButton = (Button)event.getSource();
        Stage curStage = (Stage) closeButton.getScene().getWindow();
        curStage.close();
    }

}
