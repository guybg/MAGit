package com.magit.webLogic.users;

import com.google.gson.annotations.Expose;
import com.magit.logic.exceptions.InvalidNameException;
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
    @Expose(serialize = true)private String userName;
    @Expose(serialize = true)private HashMap<String, HashMap<String,String>> repositories;
    @Expose(serialize = false) private MagitEngine engine;
    @Expose(serialize = true)private String userPath;
    @Expose(serialize = true)static final String usersPath = "c:/magit-ex3";

    public UserAccount(String userName) {
        this.userName = userName;
        this.repositories = new HashMap<>();
        userPath = Paths.get(usersPath, userName).toString();
    }

    public HashMap<String,String> getRepositoryInfo(String id) {
        return repositories.get(id);
    }

    public void addRepository(InputStream xml){
        MagitEngine engine = new MagitEngine();
        Integer serialNumber = repositories.size();
        ImportRepositoryRunnable runnable = new ImportRepositoryRunnable(xml, engine, userPath,serialNumber.toString(), null, new Consumer<HashMap<String,String>>() {
            @Override
            public void accept(HashMap<String,String> repositoryDetails) {
                repositories.put(serialNumber.toString(), repositoryDetails);
            }
        }, false);

        new Thread(runnable).start();
    }

    public void loadRepository(String id) throws InvalidNameException, ParseException, RepositoryNotFoundException, IOException {
        if(engine == null) {
            engine = new MagitEngine();
        }
        engine.updateUserName(userName);
        engine.switchRepository(Paths.get(userPath, id).toString());
    }

    public HashMap<String, HashMap<String,String>> getRepositories() {
        return repositories;
    }


}
