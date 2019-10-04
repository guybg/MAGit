package com.magit.webLogic.users;

import com.google.gson.annotations.Expose;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.Runnable.ImportRepositoryRunnable;
import com.magit.logic.system.managers.CollaborationEngine;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.system.objects.Tree;
import com.magit.logic.utils.jstree.JsTreeItem;
import com.magit.webLogic.utils.RepositoryUtils;
import com.magit.webLogic.utils.notifications.AccountNotificationsManager;
import com.magit.webLogic.utils.notifications.SingleNotification;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UserAccount {
    @Expose(serialize = true)private String userName;
    @Expose(serialize = true)private HashMap<String, HashMap<String,String>> repositories;
    @Expose(serialize = false) private HashMap<String,MagitEngine> engines;
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
        engines = new HashMap<>();
    }

    public String getUserName() {
        return userName;
    }

    public void addRepository(InputStream xml, Consumer<String> exceptionDelegate) throws InvalidNameException {
        MagitEngine engine = new MagitEngine();
        engine.updateUserName(userName);
        String serialNumber = getFreeRepositoryId();
        engines.put(serialNumber,engine);
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
        if(engines.get(id) == null) {
            engines.put(id, new MagitEngine());
            engines.get(id).updateUserName(userName);
            engines.get(id).switchRepository(Paths.get(userPath, id).toString());
        }else{
            engines.get(id).switchRepository(Paths.get(userPath, id).toString());
        }
    }

    public HashMap<String, HashMap<String,String>> getRepositories() {
        return repositories;
    }

    public void updateRepositories() throws ParseException, RepositoryNotFoundException, IOException, PreviousCommitsLimitExceededException, InvalidNameException {
        if(!Paths.get(userPath).toFile().exists())
            return;
        File[] files = new File(userPath).listFiles();
        for(File file : Objects.requireNonNull(files)){
            String id =  file.getName();
            updateRepository(id);
        }
    }

    public void updateRepository(String id) throws IOException, InvalidNameException, ParseException, RepositoryNotFoundException, PreviousCommitsLimitExceededException {
        if(repositories != null ){ //&& !repositories.containsKey(id)
            if(engines.get(id) == null)
                loadRepository(id);
            MagitEngine engine = engines.get(id);
            String commitDate="No commit",commitMessage="No commit";
            String remoteId = "none";
            String remoteUserName = "";
            String remoteRepositoryName = "";
            if(engine.getmRepositoryManager().getRepository().getRemoteReference() !=null){
                String location = engine.getmRepositoryManager().getRepository().getRemoteReference().getLocation();
                remoteId = engine.getmRepositoryManager().getRepository().getRemoteReference().getLocation().split("\\\\")[3];
                remoteUserName = engine.getmRepositoryManager().getRepository().getRemoteReference().getLocation().split("\\\\")[2];
                remoteRepositoryName = engine.getmRepositoryManager().getRepository().getRemoteReference().getRepositoryName();
            }
            HashMap<String,String> details = RepositoryUtils.setRepositoryDetailsMap(engine.getRepositoryName(), commitDate, commitMessage,remoteId,remoteUserName,remoteRepositoryName, engine);
            repositories.put(id, details);
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
                repositoryToCLoneDetails.get("commitMessage"),
                repositoryToCLoneDetails.get("remoteId"),
                repositoryToCLoneDetails.get("remoteUser"),
                repositoryToCLoneDetails.get("remoteName"),engine1));
    }

    public void setOnlineStatus(boolean status){
        online = status;
    }

    public synchronized boolean isOnline() {
        return online;
    }

    public HashMap<String, HashMap<String,String>> getRepositoryInfo(String id) throws IOException, InvalidNameException, ParseException, RepositoryNotFoundException {
        loadRepository(id);
        return engines.get(id).getRepositoryInfo(repositories.get(id));
    }

    public void deleteBranch(String branchName,String id) throws RemoteBranchException, ActiveBranchDeletedException, RepositoryNotFoundException, BranchNotFoundException, IOException, ParseException, PreviousCommitsLimitExceededException, InvalidNameException {
        engines.get(id).deleteBranch(branchName);
        updateRepository(id);
    }

    public void pickHeadBranch(String branchName,String id) throws InvalidNameException, ParseException, PreviousCommitsLimitExceededException, IOException, RepositoryNotFoundException, RemoteBranchException, UncommitedChangesException, BranchNotFoundException {
        engines.get(id).pickHeadBranch(branchName);
        updateRepository(id);
    }

    public void createRemoteTrackingBranch(String id,String remoteBranchName) throws RepositoryNotFoundException, BranchAlreadyExistsException, BranchNotFoundException, InvalidNameException, RemoteReferenceException, IOException {
        engines.get(id).createRemoteTrackingBranch(remoteBranchName);
    }

    public void forcedChangeBranch(String id,String branchName) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        engines.get(id).forcedChangeBranch(branchName);
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

    public synchronized void addNotification(String userName, String message){
        notificationsManager.addNotification(new SingleNotification(message, userName));
    }

    public HashMap<String,String> createBranch(String branchName, String id) throws BranchAlreadyExistsException, InvalidNameException, RepositoryNotFoundException, IOException {
        Branch newBranch = engines.get(id).createNewBranch(branchName);
        HashMap<String, String> branchInfo = new HashMap<>();
        branchInfo.put("Name",newBranch.getBranchName());
        branchInfo.put("Commit",newBranch.getPointedCommitSha1().toString());
        branchInfo.put("IsRemote",newBranch.getIsRemote().toString());
        branchInfo.put("IsTracking",newBranch.getIsTracking().toString());
        branchInfo.put("TrackingAfter",newBranch.getTrackingAfter());
        return branchInfo;
    }

    public TreeMap<Date, HashMap<String,String>> getCommitsInfo(String id) throws IOException, ParseException, PreviousCommitsLimitExceededException, CommitNotFoundException {
        TreeMap<Date,HashMap<String,String>> treeMap = new TreeMap<>(Collections.reverseOrder());
        HashMap<String, HashMap<String,String>> commits = new HashMap<>();
        Path pathToObjects = engines.get(id).getmRepositoryManager().getRepository().getObjectsFolderPath();
        ArrayList<String> sha1s = engines.get(id).getHeadBranchCommits();
        for (String sha1 : sha1s) {
            Commit currentCommit = Commit.createCommitInstanceByPath(Paths.get(pathToObjects.toString(), sha1));
            HashMap<String,String> commitInfo = currentCommit.toHashMap();
            commits.put(sha1, commitInfo);
            treeMap.put(currentCommit.getCreationDate(), commitInfo);
        }

        List<String> branches = engines.get(id).getBranches().stream().map(b -> b.getBranchName()).collect(Collectors.toList());
        String pathToBranches = engines.get(id).getmRepositoryManager().getRepository().getBranchDirectoryPath().toString();
        for (String branchName : branches) {
            Path pathToBranch = Paths.get(pathToBranches, branchName);
            if (Files.notExists(pathToBranch))
                continue;

            File branchFile = new File(pathToBranch.toString());
            String sha1 = Repository.readBranchContent(branchFile).get("sha1");
            if (!commits.containsKey(sha1))
                continue;

            String branchesOfCommit = commits.get(sha1).get("Branches");
            String valueToInsert = null;
            if (branchesOfCommit.equals(""))
                valueToInsert = branchName;
            else
                valueToInsert = String.format("%s, %s", branchesOfCommit, branchName);
            commits.get(sha1).put("Branches", valueToInsert);
        }
        return treeMap;
    }

    public void createPullRequest(UserAccount receiverUser,String engineIdOfReceiver, String targetBranchName,String baseBranchName,String message,String engineId) throws IOException, RepositoryNotFoundException, RemoteReferenceException, PushException, UnhandledMergeException, CommitNotFoundException, ParseException, UncommitedChangesException, RemoteBranchException, PreviousCommitsLimitExceededException, BranchNotFoundException, PullRequestException {
        receiverUser.engines.get(engineIdOfReceiver).createPullRequest(engines.get(engineId),targetBranchName,baseBranchName,message);
    }

    public void acceptPullRequest(String engineId,int pullRequestId) throws UnhandledMergeException, MergeNotNeededException, RepositoryNotFoundException, MergeException, UncommitedChangesException, FastForwardException, InvalidNameException, ParseException, PreviousCommitsLimitExceededException, IOException, BranchNotFoundException, RemoteBranchException, WorkingCopyStatusNotChangedComparedToLastCommitException, UnhandledConflictsException, WorkingCopyIsEmptyException {
        engines.get(engineId).acceptPullRequest(pullRequestId);
    }

    public void rejectPullRequest(String engineId,int pullRequestId) throws UnhandledMergeException, MergeNotNeededException, RepositoryNotFoundException, MergeException, UncommitedChangesException, FastForwardException {
        engines.get(engineId).rejectPullRequest(pullRequestId);
    }

    public void push(String repositoryId) throws IOException, RemoteReferenceException, UncommitedChangesException, PushException, UnhandledMergeException, ParseException, CommitNotFoundException, RemoteBranchException, PreviousCommitsLimitExceededException {
        engines.get(repositoryId).push();
    }

    public void pull(String repositoryId) throws PreviousCommitsLimitExceededException, RepositoryNotFoundException, FastForwardException, UncommitedChangesException, MergeException, UnhandledMergeException, ParseException, CommitNotFoundException, IOException, RemoteBranchException, RemoteReferenceException, MergeNotNeededException {
        engines.get(repositoryId).pull();
    }

    public ArrayList<CollaborationEngine.PullRequest> getPullRequests(String id){
        return engines.get(id).getCollaborationEngine().getPullRequests();
    }

    public ArrayList<JsTreeItem> getTree(String id, String sha1) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        return engines.get(id).getTree(sha1);
    }

    public ArrayList<JsTreeItem> getPullRequestDifferencesJsTreeArray(String id) throws PreviousCommitsLimitExceededException, IOException, ParseException, RepositoryNotFoundException {
        return engines.get(id).getWorkingCopyStatusJsTree();
    }

    public ArrayList<JsTreeItem> getOverallCommitsDiff(String target, String base, String repoId) throws PreviousCommitsLimitExceededException, IOException, CommitNotFoundException, ParseException, RepositoryNotFoundException {
        return engines.get(repoId).getOverallCommitsDiff(base ,target);
    }

    public ArrayList<JsTreeItem> getTree(String id) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        return engines.get(id).getTree();
    }

    public void deleteFile(String id, String path) throws IOException {
        engines.get(id).deleteFile(path);
    }

    public void renameFile(String id, String path, String newFileName) {
        engines.get(id).renameFile(path, newFileName);
    }

    public boolean createFile(String id, String path) throws IOException {
        return engines.get(id).createFile(path);
    }

    public void createFolder(String id, String path) {
        engines.get(id).createFolder(path);
    }

    public void saveContentToFile(String id, String path, String data) throws IOException {
        engines.get(id).saveContentToFile(path, data);
    }

    public void commit(String id,String inputFromUser) throws WorkingCopyStatusNotChangedComparedToLastCommitException, UnhandledConflictsException, ParseException, PreviousCommitsLimitExceededException, IOException, RepositoryNotFoundException, WorkingCopyIsEmptyException, FastForwardException {
        engines.get(id).commit(inputFromUser);
    }
}