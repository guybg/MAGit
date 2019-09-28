package com.magit.webLogic.users;

import com.google.gson.annotations.Expose;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.Runnable.ImportRepositoryRunnable;
import com.magit.logic.system.objects.Branch;
import com.magit.webLogic.utils.RepositoryUtils;
import com.magit.webLogic.utils.notifications.AccountNotificationsManager;
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
    @Expose(serialize = true) private AccountNotificationsManager notificationsManager;

    public UserAccount(String userName) {
        this.userName = userName;
        this.repositories = new HashMap<>();
        this.online = true;
        userPath = Paths.get(usersPath, userName).toString();
        notificationsManager = new AccountNotificationsManager();
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

    public void setLastUpdatedNotificationsVersion(Integer notificationsVersion){
        notificationsManager.setLastUpdatedNotificationsVersion(notificationsVersion);
    }

    public Integer getLastUpdatedNotificationsVersion(){
        return notificationsManager.getLastUpdatedNotificationsVersion();
    }

    public Integer getNotificationsVersion(){
        return notificationsManager.getNotificationsVersion();
    }

    public List<SingleNotification> getNotifications(Integer fromVersion){
        return notificationsManager.getNotifications(fromVersion);
    }

    public Integer getNumberOfNewNotifications(){
        return notificationsManager.getUnseenNotificationsAmount();
    }

    public void onLogout(){
        notificationsManager.updateLastUpdatedNotificationsVersion();
    }

    public synchronized void addNotification(String message, String userName){
        notificationsManager.addNotification(new SingleNotification(message, userName));
    }

    public HashMap<String,String> createBranch(String branchName) throws BranchAlreadyExistsException, InvalidNameException, RepositoryNotFoundException, IOException {
        Branch newBranch = engine.createNewBranch(branchName);
        HashMap<String, String> branchInfo = new HashMap<>();
        branchInfo.put("Name",newBranch.getBranchName());
        branchInfo.put("Commit",newBranch.getPointedCommitSha1().toString());
        branchInfo.put("IsRemote",newBranch.getIsRemote().toString());
        branchInfo.put("IsTracking",newBranch.getIsTracking().toString());
        branchInfo.put("TrackingAfter",newBranch.getTrackingAfter());
        return branchInfo;
    }

    public void createRemoteTrackingBranch(String branchName) throws RepositoryNotFoundException, BranchAlreadyExistsException, BranchNotFoundException, InvalidNameException, RemoteReferenceException, IOException {
        engine.createRemoteTrackingBranch(branchName);
    }
}
