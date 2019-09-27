package com.magit.webLogic.users;

import com.google.gson.annotations.Expose;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.Runnable.ImportRepositoryRunnable;
import com.magit.webLogic.utils.RepositoryUtils;
import com.magit.webLogic.utils.notifications.SingleNotification;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class UserAccount {
    @Expose(serialize = true)private String userName;
    @Expose(serialize = true)private HashMap<String, HashMap<String,String>> repositories;
    @Expose(serialize = false) private MagitEngine engine;
    @Expose(serialize = true)private String userPath;
    @Expose(serialize = true)static final String usersPath = "c:/magit-ex3";
    @Expose(serialize = true) private boolean online;
    @Expose(serialize = true) private HashMap<Integer, SingleNotification> notifications;
    @Expose(serialize = true) private Integer notificationsVersion = 0;
    public UserAccount(String userName) {
        this.userName = userName;
        this.repositories = new HashMap<>();
        this.online = true;
        userPath = Paths.get(usersPath, userName).toString();
    }

    public void addRepository(InputStream xml, Consumer<String> exceptionDelegate){
        MagitEngine engine = new MagitEngine();
        String serialNumber = getFreeRepositoryId();
        ImportRepositoryRunnable runnable = new ImportRepositoryRunnable(xml, engine, userPath, serialNumber, null, new Consumer<String>() {
            @Override
            public void accept(String s) {
                exceptionDelegate.accept(s);
            }
        }, new Consumer<HashMap<String, String>>() {
            @Override
            public void accept(HashMap<String, String> repositoryDetails) {
                repositories.put(serialNumber, repositoryDetails);
            }
        }, false);
        runnable.run();
        //new Thread(runnable).start();
    }

    private synchronized String getFreeRepositoryId(){
        Integer serialNumber = repositories.size();
        return serialNumber.toString();
    }
    public synchronized List<SingleNotification> getNotifications(Integer fromVersion){
        if(fromVersion < notificationsVersion){
            fromVersion = notificationsVersion;
        }
        if (fromVersion < 0 || fromVersion > notifications.size()) {
            fromVersion = 0;
        }


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

    public void cloneRepository(UserAccount accountToCloneFrom, String repositoryIdToClone, String cloneName) throws CloneException, InvalidNameException, IllegalPathException, IOException, ParseException, PreviousCommitsLimitExceededException, RepositoryNotFoundException {
        HashMap<String,String> repositoryToCLoneDetails = accountToCloneFrom.getRepositories().get(repositoryIdToClone);
        String repositoryToCloneName = repositoryToCLoneDetails.get("name");
        MagitEngine engine1 = new MagitEngine();
        String toClonePath = Paths.get("c:/magit-ex3", accountToCloneFrom.userName,repositoryIdToClone).toString();
        String serialNumber = getFreeRepositoryId();
        String clonedPath = Paths.get("c:/magit-ex3", userName,serialNumber).toString();

        engine1.clone(toClonePath,clonedPath,cloneName);
        engine1.switchRepository(clonedPath);
        repositories.put(serialNumber,RepositoryUtils.setRepositoryDetailsMap(cloneName,
                repositoryToCLoneDetails.get("commitDate"),
                repositoryToCLoneDetails.get("commitMessage"),engine1));
    }

    public void setOnlineStatus(boolean status){
        online = status;
    }

    public void setNotificationsVersion(Integer notificationsVersion) {
        this.notificationsVersion = notificationsVersion;
    }

    public Integer getNotificationsVersion() {
        return notificationsVersion;
    }

    public synchronized boolean isOnline() {
        return online;
    }

    public HashMap<String, HashMap<String,String>> getRepositoryInfo(String id) {
        return engine.getRepositoryInfo(repositories.get(id));
    }

    public void deleteBranch(String branchName) throws RemoteBranchException, ActiveBranchDeletedException, RepositoryNotFoundException, BranchNotFoundException, IOException {
        engine.deleteBranch(branchName);
    }

    public void pickHeadBranch(String branchName) throws InvalidNameException, ParseException, PreviousCommitsLimitExceededException, IOException, RepositoryNotFoundException, RemoteBranchException, UncommitedChangesException, BranchNotFoundException {
        engine.pickHeadBranch(branchName);
    }
}
