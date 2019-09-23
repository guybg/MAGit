package com.magit.webLogic.users;

import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.Runnable.ImportRepositoryRunnable;
import com.magit.logic.system.tasks.ImportRepositoryTask;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

public class UserAccount {
    private String userName;
    private HashMap<String, HashMap<String,String>> repositories;
    private MagitEngine engine;
    private String userPath;
    static final String usersPath = "c:/magit-ex3";

    public UserAccount(String userName) {
        this.userName = userName;
        this.repositories = new HashMap<>();
        userPath = Paths.get(usersPath, userName).toString();
    }

    public void addRepository(InputStream xml){
        MagitEngine engine = new MagitEngine();
        ImportRepositoryRunnable runnable = new ImportRepositoryRunnable(xml, engine, userPath, null, new Consumer<HashMap<String,String>>() {
            @Override
            public void accept(HashMap<String,String> repositoryDetails) {
                repositories.put(repositoryDetails.get("name"), repositoryDetails);
            }
        }, false);

        new Thread(runnable).start();
    }

    public void loadRepository(String repositoryName) throws ParseException, RepositoryNotFoundException, IOException {
        engine = new MagitEngine();
        engine.switchRepository(Paths.get(userPath,repositoryName).toString());
    }

    public HashMap<String, HashMap<String,String>> getRepositories() {
        return repositories;
    }


}
