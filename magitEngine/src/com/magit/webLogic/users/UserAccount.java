package com.magit.webLogic.users;

import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.Runnable.ImportRepositoryRunnable;
import com.magit.logic.system.tasks.ImportRepositoryTask;
import javafx.beans.property.SimpleStringProperty;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

public class UserAccount {
    String userName;
    HashMap<String, String> repositories;



    public UserAccount(String userName) {
        this.userName = userName;
        this.repositories = new HashMap<>();
        repositories.put("c:/banana", "banana");
        repositories.put("c:/banana1", "banana1");
        repositories.put("c:/banana2", "banana2");
        repositories.put("c:/banana3", "banana3");
        repositories.put("c:/banana4", "banana4");
    }

    static final String usersPath = "c:/magit-ex3";
    public void addRepository(InputStream xml){
        final String userPath = Paths.get(usersPath, userName).toString();
        MagitEngine engine = new MagitEngine();
        //String filePath, MagitEngine engine,
        // StringProperty repositoryNameProperty,
        // StringProperty repositoryPathProperty,
        // Runnable forceCreationRunnable,Runnable doAfter,
        // boolean forceCreation
        ImportRepositoryRunnable runnable = new ImportRepositoryRunnable(xml, engine, userPath, null, new Consumer<String>() {
            @Override
            public void accept(String repositoryName) {
                repositories.put(Paths.get(userPath,repositoryName).toString(), repositoryName);
            }
        }, false);

        new Thread(runnable).start();
    }

    public HashMap<String, String> getRepositories() {
        return repositories;
    }


}
