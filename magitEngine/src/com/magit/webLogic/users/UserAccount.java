package com.magit.webLogic.users;

import com.google.gson.annotations.Expose;
import com.magit.logic.exceptions.InvalidNameException;
import com.magit.logic.exceptions.PreviousCommitsLimitExceededException;
import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.Runnable.ImportRepositoryRunnable;
import com.magit.logic.system.Runnable.ImportRepositoryRunnable;
import com.magit.logic.system.tasks.ImportRepositoryTask;
import com.magit.webLogic.utils.RepositoryUtils;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Consumer;

public class UserAccount {
    @Expose(serialize = true)private String userName;
    @Expose(serialize = true)private HashMap<String, HashMap<String,String>> repositories;
    @Expose(serialize = false) private MagitEngine engine;
    @Expose(serialize = true)private String userPath;
    @Expose(serialize = true)static final String usersPath = "c:/magit-ex3";
    @Expose(serialize = true) private boolean online;

    public UserAccount(String userName) {
        this.userName = userName;
        this.repositories = new HashMap<>();
        this.online = true;
        userPath = Paths.get(usersPath, userName).toString();
    }

    public HashMap<String,String> getRepositoryInfo(String id) {
        return repositories.get(id);
    }

    public void addRepository(InputStream xml, Consumer<String> exceptionDelegate){
        MagitEngine engine = new MagitEngine();
        Integer serialNumber = repositories.size();
        ImportRepositoryRunnable runnable = new ImportRepositoryRunnable(xml, engine, userPath, serialNumber.toString(), null, new Consumer<String>() {
            @Override
            public void accept(String s) {
                exceptionDelegate.accept(s);
            }
        }, new Consumer<HashMap<String, String>>() {
            @Override
            public void accept(HashMap<String, String> repositoryDetails) {
                repositories.put(serialNumber.toString(), repositoryDetails);
            }
        }, false);
        runnable.run();
        //new Thread(runnable).start();
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

    public void updateRepositories() throws ParseException, RepositoryNotFoundException, IOException, PreviousCommitsLimitExceededException {
        if(!Paths.get(userPath).toFile().exists())
            return;
        File[] files = new File(userPath).listFiles();
        for(File file : Objects.requireNonNull(files)){
            String id =  file.getName();
            if(repositories != null && !repositories.containsKey(id)){
                MagitEngine engine = new MagitEngine();
                String commitDate="No commit",commitMessage="No commit";
                engine.switchRepository(Paths.get(userPath, id).toString());
                HashMap<String,String> details = RepositoryUtils.setRepositoryDetailsMap(engine.getRepositoryName(), commitDate, commitMessage, engine);
                repositories.put(id, details);
            }
        }
    }

    public void setOnlineStatus(boolean status){
        online = status;
    }

    public synchronized boolean isOnline() {
        return online;
    }
}
