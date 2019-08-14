package com.magit.properties;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserName {
    final private StringProperty userName = new SimpleStringProperty();

    public StringProperty valueProperty() {
        return userName;
    }

    //the getter and setter are used for backward compatibility and are not mandatory at all
    public String getValue() {
        return userName.get();
    }

    public void setValue(String name) {
        userName.set(name);
    }
}
