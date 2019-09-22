package com.magit.webLogic.users;

import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.tasks.ImportRepositoryTask;
import javafx.beans.property.SimpleStringProperty;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

public class UserAccount {
    String userName;
    HashMap<String, String> repositories;

    public UserAccount(String userName) {
        this.userName = userName;
        this.repositories = new HashMap<>();
    }

    static final String usersPath = "c:/magit-ex3";
    public void addRepository(String xmlPath, String repositoryName){
        final String userPath = Paths.get(usersPath, userName).toString(), repositoryPath = Paths.get(userPath,repositoryName).toString();
        MagitEngine engine = new MagitEngine();
        //String filePath, MagitEngine engine,
        // StringProperty repositoryNameProperty,
        // StringProperty repositoryPathProperty,
        // Runnable forceCreationRunnable,Runnable doAfter,
        // boolean forceCreation
        ImportRepositoryTask task = new ImportRepositoryTask(xmlPath, engine, new SimpleStringProperty(), new SimpleStringProperty(), null, new Runnable() {
            @Override
            public void run() {
                //success
                repositories.put(repositoryPath, repositoryName);
            }
        }, false);

        new Thread(task).start();
    }

    public HashMap<String, String> getRepositories() {
        return repositories;
    }


}
