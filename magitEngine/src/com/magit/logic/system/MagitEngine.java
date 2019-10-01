package com.magit.logic.system;

import com.fxgraph.graph.Model;
import com.magit.controllers.BranchesHistoryScreenController;
import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.managers.BranchManager;
import com.magit.logic.system.managers.CollaborationEngine;
import com.magit.logic.system.managers.MergeEngine;
import com.magit.logic.system.managers.RepositoryManager;
import com.magit.logic.system.objects.*;
import com.magit.logic.system.tasks.BranchesHistoryTask;
import com.magit.logic.system.tasks.CollectFileItemsInfoTask;
import com.magit.logic.system.tasks.NewCommitTask;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.utils.compare.Delta.DeltaFileItem;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.FileItemHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import com.magit.logic.visual.node.CommitNode;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MagitEngine {

    private final String BLANK_SPACE = " \t\u00A0\u1680\u180e\u2000\u200a\u202f\u205f\u3000\u2800";
    private final RepositoryManager mRepositoryManager;
    private final BranchManager mBranchManager;
    private String mUserName = "Administrator";
    private MergeEngine mergeEngine;
    private CollaborationEngine collaborationEngine;

    public MagitEngine() {
        mRepositoryManager = new RepositoryManager();
        mBranchManager = new BranchManager();
        mergeEngine = new MergeEngine();
        collaborationEngine = new CollaborationEngine();
    }

    public RepositoryManager getmRepositoryManager() {
        return mRepositoryManager;
    }

    public BranchManager getmBranchManager() {
        return mBranchManager;
    }

    public String getRepositoryName(){
        if(mRepositoryManager.getRepository() != null)
            return mRepositoryManager.getRepository().getRepositoryName();
        else
            return "No repository";
    }

    public void updateUserName(String userNameToSet) throws InvalidNameException {
        if (StringUtils.containsOnly(userNameToSet, BLANK_SPACE) || userNameToSet.isEmpty())
            throw new InvalidNameException("Username should contain at least one alphanumeric character from A–Z or 0–9 or any symbol that is not a blank space");
        mUserName = userNameToSet;
    }

    public void repositoryNotFoundCheck() throws RepositoryNotFoundException {
        if (mRepositoryManager.getRepository() == null)
            throw new RepositoryNotFoundException("Please load or create a repository before trying this operation");
    }

    public String guiGetRepositoryPath(){
        try {
            repositoryNotFoundCheck();
            return mRepositoryManager.getRepository().getRepositoryPath().toAbsolutePath().toString();
        } catch (RepositoryNotFoundException e) {
            return "";
        }

    }

    public void loadHeadBranchCommitFiles() throws JAXBException, IOException, ParseException, PreviousCommitsLimitExceededException, XmlFileException, IllegalPathException, RepositoryAlreadyExistsException {
        mRepositoryManager.unzipHeadBranchCommitWorkingCopy();
    }

    public void exportRepositoryToXML(String path, String fileName) throws IOException, ParseException, PreviousCommitsLimitExceededException
            , JAXBException, IllegalPathException {
      //  try {
          //  Path fullPath = Paths.get(path, fileName.concat(".xml"));
          //  FileHandler.writeNewFolder(Paths.get(path).toString());
            //RepositoryXmlParser parser = new RepositoryXmlParser();
            //parser.writeRepositoryToXML(mRepositoryManager.getRepository(), fullPath.toAbsolutePath().toString());
       // } catch (IllegalArgumentException | IOException e) {
       ///     throw new IllegalPathException("Invalid file path: " + e.getMessage());
       // }
    }

    public void switchRepository(String pathOfRepository) throws IOException, ParseException, RepositoryNotFoundException {
        mRepositoryManager.switchRepository(Paths.get(pathOfRepository).toAbsolutePath().toString(), mBranchManager, mUserName);

    }

    public String presentCurrentCommitAndHistory() throws IOException, ParseException, RepositoryNotFoundException, CommitNotFoundException, PreviousCommitsLimitExceededException {
        repositoryNotFoundCheck();
        return mRepositoryManager.presentCurrentCommitAndHistory();
    }

    public String changeBranchPointedCommit(String commitSha1) throws IOException, CommitNotFoundException, ParseException, RepositoryNotFoundException, PreviousCommitsLimitExceededException {
        repositoryNotFoundCheck();
        mBranchManager.changeBranchPointedCommit(mRepositoryManager.getRepository(), new Sha1(commitSha1, true));
        FileHandler.clearFolder(mRepositoryManager.getRepository().getRepositoryPath());
        deleteBranchMergeFolder();
        mRepositoryManager.unzipHeadBranchCommitWorkingCopy();
        return mRepositoryManager.presentCurrentCommitAndHistory();
    }

    public void workingCopyChangedComparedToCommit() throws ParseException, PreviousCommitsLimitExceededException, IOException, RepositoryNotFoundException, UncommitedChangesException {
        repositoryNotFoundCheck();
        if (mRepositoryManager.getRepository().areThereChanges(mRepositoryManager.checkDifferenceBetweenCurrentWCAndLastCommit()))
            throw new UncommitedChangesException("There are unsaved changes");
    }

    public void commit(String inputFromUser) throws IOException, WorkingCopyIsEmptyException, ParseException, RepositoryNotFoundException, UnhandledConflictsException,
            WorkingCopyStatusNotChangedComparedToLastCommitException, PreviousCommitsLimitExceededException, FastForwardException {
        repositoryNotFoundCheck();
        if(mergeEngine.headBranchHasMergeConflicts(mRepositoryManager.getRepository())){
            throw new UnhandledConflictsException("Please solve conflicts before committing changes.");
        }
        try {
            mRepositoryManager.commit(inputFromUser, mUserName, mBranchManager.getActiveBranch());
        }catch (FastForwardException e){
            deleteBranchMergeFolder();
            throw new FastForwardException(e.getMessage());
        }
        deleteBranchMergeFolder();
    }

    private void deleteBranchMergeFolder(){
        if(mergeEngine.headBranchHasUnhandledMerge(mRepositoryManager.getRepository())){
            FileUtils.deleteQuietly(Paths.get(mRepositoryManager.getRepository().getMagitFolderPath().toString(),".merge", mBranchManager.getActiveBranch().getBranchName()).toFile());
        }
    }
    public void guiCommit(Consumer<String> exceptionDelegate, Runnable onSuccess, String inputFromUser){
        NewCommitTask task = new NewCommitTask(onSuccess, this, inputFromUser);
        new Thread(task).start();
        task.setOnFailed(event -> exceptionDelegate.accept(task.getException().getMessage()));
    }


    public String getBranchesInfo() throws IOException, RepositoryNotFoundException, ParseException, PreviousCommitsLimitExceededException {
        repositoryNotFoundCheck();
        return mRepositoryManager.getBranchesInfo();
    }

    public ObservableList<Branch> getBranches() {
        return mRepositoryManager.getBranches();
    }



    public String getHeadBranchName() {
        return mRepositoryManager.getHeadBranch();
    }

    public String getUserName() {
        return mUserName;
    }

    public void createNewRepository(Path pathToFile, String repositoryName) throws IllegalPathException, InvalidNameException, RepositoryAlreadyExistsException {
        try {
            if (StringUtils.containsOnly(repositoryName, BLANK_SPACE) || repositoryName.isEmpty())
                throw new InvalidNameException("Repository name should contain at least one alphanumeric character from A–Z or 0–9 or any symbol that is not a blank space");
            mRepositoryManager.createNewRepository(pathToFile.toString(), mBranchManager, repositoryName);
        } catch (NullPointerException | InvalidPathException | IOException e) {
            throw new IllegalPathException(pathToFile.toString() + " is not a valid path.");
        }
    }

    public Branch createNewBranch(String branchName) throws IOException, RepositoryNotFoundException, InvalidNameException, BranchAlreadyExistsException {
        repositoryNotFoundCheck();
        return mBranchManager.createNewBranch(branchName, mRepositoryManager.getRepository(),false,false,null);
    }

    public void createNewBranch(String branchName, String sha1OfCommit) throws IOException, RepositoryNotFoundException, InvalidNameException, BranchAlreadyExistsException {
        repositoryNotFoundCheck();
        mBranchManager.createNewBranch(branchName, mRepositoryManager.getRepository(),sha1OfCommit,false,false,null);
    }

    public void createNewBranch(String branchName, String sha1OfCommit, String trackingAfter) throws IOException, RepositoryNotFoundException, InvalidNameException, BranchAlreadyExistsException {
        repositoryNotFoundCheck();
        mBranchManager.createNewBranch(branchName, mRepositoryManager.getRepository(),sha1OfCommit,false,true,trackingAfter);
    }
    public ArrayList<String> getRemoteBranchesOfCommit(String sha1) throws BranchNotFoundException {
        ArrayList<String> remoteBranches = mRepositoryManager.getRepository().getBranches().values().stream().filter(b -> b.getPointedCommitSha1().toString().equals(sha1)).filter(Branch::getIsRemote).map(Branch::getBranchName).collect(Collectors.toCollection(ArrayList::new));
        if(remoteBranches.size() == 0)
            throw new BranchNotFoundException("","There are no remote branches on selected commit");
        return remoteBranches;
    }

    public ArrayList<String> getNonRemoteBranchesOfCommit(String sha1) throws BranchNotFoundException {
        ArrayList<String> remoteBranches = mRepositoryManager.getRepository().getBranches().values().stream().filter(b -> b.getPointedCommitSha1().toString().equals(sha1)).filter(branch -> branch.getIsRemote().equals(false)).map(Branch::getBranchName).collect(Collectors.toCollection(ArrayList::new));
        if(remoteBranches.size() == 0)
            throw new BranchNotFoundException("","There are no Non-Remote branches on selected commit");
        return remoteBranches;
    }
    public ArrayList<Branch> getNonRemoteBranches(){
        return getBranches().stream().filter(b->b.getIsRemote().equals(false)).collect(Collectors.toCollection(ArrayList::new));
    }
    public void deleteBranch(String branchNameToDelete) throws IOException, ActiveBranchDeletedException, RepositoryNotFoundException, BranchNotFoundException, RemoteBranchException {
        repositoryNotFoundCheck();
        mBranchManager.deleteBranch(branchNameToDelete, mRepositoryManager.getRepository());
    }

    public ArrayList<String> getLastCommitDateAndMessage() throws IOException, ParseException, PreviousCommitsLimitExceededException {
        Commit commit = Commit.createCommitInstanceByPath(mRepositoryManager.getRepository().getCommitPath());
        if(commit == null) return null;
        ArrayList<String> commitInfo = new ArrayList<>();
        commitInfo.add(commit.getCreationDate().toString());
        commitInfo.add(commit.getCommitMessage());
        return commitInfo;
    }

    public String pickHeadBranch(String branchName) throws IOException, ParseException, RepositoryNotFoundException, BranchNotFoundException, UncommitedChangesException, PreviousCommitsLimitExceededException, InvalidNameException, RemoteBranchException {
        repositoryNotFoundCheck();
        if ((StringUtils.containsAny(branchName, BLANK_SPACE) || branchName.isEmpty()) && !mRepositoryManager.getRepository().getBranches().get(branchName).getIsRemote())
            throw new InvalidNameException("Branch name cannot be empty or with whitespace.");
        return mBranchManager.pickHeadBranch(branchName,
                mRepositoryManager.getRepository(), mRepositoryManager.checkDifferenceBetweenCurrentWCAndLastCommit());
    }

    public String presentCurrentBranch() throws IOException, ParseException, RepositoryNotFoundException, PreviousCommitsLimitExceededException {
        repositoryNotFoundCheck();
        return mBranchManager.presentCurrentBranch(mRepositoryManager.getRepository());
    }

    public TreeSet<CommitNode> guiBranchesHistory(Model model, BranchesHistoryScreenController branchesHistoryScreenController, boolean allCommits) throws ParseException, PreviousCommitsLimitExceededException, IOException {
       return mBranchManager.guiPresentBranchesHistory(mRepositoryManager.getRepository(),model, branchesHistoryScreenController,allCommits);
    }

    public void guiBranchesHistory(Consumer<TreeSet<CommitNode>> infoReadyDelegate, Consumer<String> exceptionHandleDelegate, Model model, BranchesHistoryScreenController branchesHistoryScreenController){
        BranchesHistoryTask task = new BranchesHistoryTask(infoReadyDelegate, this, model,branchesHistoryScreenController);
        new Thread(task).start();
        task.setOnFailed(event -> exceptionHandleDelegate.accept(task.getException().getMessage()));
    }

    public String getWorkingCopyStatus() throws IOException, ParseException, RepositoryNotFoundException, PreviousCommitsLimitExceededException {
        repositoryNotFoundCheck();
        return mRepositoryManager.getWorkingCopyStatus(mUserName);
    }

    public String forcedChangeBranch(String branchName) throws ParseException, IOException, PreviousCommitsLimitExceededException {
        return mBranchManager.forcedChangeBranch(branchName,
                mRepositoryManager.getRepository());
    }

    public Map<FileStatus, SortedSet<DeltaFileItem>> getWorkingCopyStatusMap() throws IOException, ParseException, RepositoryNotFoundException, PreviousCommitsLimitExceededException {
        repositoryNotFoundCheck();
        return mRepositoryManager.checkDifferenceBetweenCurrentWCAndLastCommit();
    }

    public Map<FileStatus, SortedSet<Delta.DeltaFileItem>> getDifferencesBetweenTwoCommits(String sha1OfFirstCommit, String sha1OfSecondCommit) throws IOException, ParseException, RepositoryNotFoundException, PreviousCommitsLimitExceededException {
        repositoryNotFoundCheck();
        return mRepositoryManager.checkDifferencesBetweenTwoCommits(sha1OfFirstCommit,sha1OfSecondCommit);
    }

    public ArrayList<String> guiGetAllCommitsOfRepository() throws IOException {
        return mRepositoryManager.guiGetRepositoryCommitList();
    }

    public void guiCollectCommitHistoryInfo(Consumer<ObservableList<FileItemInfo>> infoReadyDelegate, Consumer<String> exceptionHandleDelegate){
        CollectFileItemsInfoTask collectFileItemsInfoTask = new CollectFileItemsInfoTask(infoReadyDelegate, mRepositoryManager);
        new Thread(collectFileItemsInfoTask).start();

        collectFileItemsInfoTask.setOnFailed(event1 -> exceptionHandleDelegate.accept(collectFileItemsInfoTask.getException().getMessage()));
    }

    public String guiGetBranchInfo(Branch branch) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        return mRepositoryManager.guiGetBranchInfo(branch);
    }

    public void merge(String branchName,boolean pullOperation) throws UnhandledMergeException, MergeNotNeededException, FastForwardException, MergeException, UncommitedChangesException, RepositoryNotFoundException {
        try {
            workingCopyChangedComparedToCommit();
            mergeEngine.merge(mRepositoryManager.getRepository(), mRepositoryManager.getRepository().getBranches().get(branchName),pullOperation);
        } catch (ParseException | PreviousCommitsLimitExceededException | IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<FileStatus, ArrayList<FileItemInfo>> getMergeOpenChanges() throws PreviousCommitsLimitExceededException, RepositoryNotFoundException, ParseException, IOException {
        return mergeEngine.getOpenChanges(mRepositoryManager.getRepository());
    }



    public ArrayList<ConflictItem> getMergeConflicts(){
        if(mergeEngine.headBranchHasMergeConflicts(mRepositoryManager.getRepository())){
            return mergeEngine.getConflictItems(mRepositoryManager.getRepository());
        }
        return null;
    }

    public void updateSolvedConflict(String path,  String fileContent, boolean deleted){
        mergeEngine.saveSolvedConflictItem(path,fileContent,mRepositoryManager.getRepository(),deleted);
    }

    public String getMergedWithBranchNameFromUnhandledMerge() throws IOException {
        String content = FileHandler.readFile(Paths.get(mRepositoryManager.getRepository().getMagitFolderPath().toString()
                ,".merge", mBranchManager.getActiveBranch().getBranchName(),"merge-info").toString());
        return content.split(System.lineSeparator())[3];

    }

    public void activeBranchHasUnhandledMerge() throws UnhandledMergeException {
        if(mRepositoryManager.getRepository().headBranchHasUnhandledMerge())
            throw new UnhandledMergeException("Unhandled merge, loading information.");
    }

    public boolean headBranchHasMergeConflicts(){
        return mergeEngine.headBranchHasMergeConflicts(mRepositoryManager.getRepository());
    }
    public boolean headBranchHasMergeOpenChanges(){
        return mergeEngine.headBranchHasMergeOpenChanges(mRepositoryManager.getRepository());
    }

    public void clone(String toClonePath, String clonedPath, String cloneName) throws IOException, IllegalPathException, CloneException, InvalidNameException {
        collaborationEngine.cloneRepository(toClonePath,clonedPath,cloneName,new BranchManager());
    }

    public void fetch() throws PreviousCommitsLimitExceededException, RemoteReferenceException, CommitNotFoundException, ParseException, IOException, IllegalPathException {
        collaborationEngine.fetch(mRepositoryManager.getRepository());
    }

    public void pull() throws ParseException, PreviousCommitsLimitExceededException, IOException, MergeNotNeededException, UnhandledMergeException, FastForwardException, RemoteReferenceException, CommitNotFoundException, UncommitedChangesException, RepositoryNotFoundException, RemoteBranchException, MergeException {
        collaborationEngine.pull(this);
    }

    public void push() throws IOException, UnhandledMergeException, RemoteReferenceException, PushException, RemoteBranchException, CommitNotFoundException, ParseException, UncommitedChangesException, PreviousCommitsLimitExceededException {
        collaborationEngine.push(this);
    }

    public void createRemoteTrackingBranch(String remoteBranchName) throws BranchNotFoundException, RepositoryNotFoundException, InvalidNameException, BranchAlreadyExistsException, RemoteReferenceException, IOException {
        repositoryNotFoundCheck();
        if (!mRepositoryManager.getRepository().getBranches().containsKey(remoteBranchName))
            throw new BranchNotFoundException("Remote branch does not exist");

        mBranchManager.createRemoteTrackingBranch(mRepositoryManager.getRepository().getBranches().get(remoteBranchName), mRepositoryManager.getRepository());
    }

    public boolean repositoryHasRemoteReference() throws RepositoryNotFoundException {
        repositoryNotFoundCheck();
        return mRepositoryManager.hasRemoteReference();
    }

    public boolean activeBranchIsTrackingAfter(){
        return mBranchManager.activeBranchIsTrackingAfter();
    }

    public HashMap<String, HashMap<String, String>> getRepositoryInfo(HashMap<String, String> repDetails) {
        HashMap<String, HashMap<String, String>> repositoryInfo = new HashMap<>();
        repositoryInfo.put("Repository", repDetails);
        for (Branch branch : getBranches()) {
            repositoryInfo.put(branch.getBranchName(), new HashMap<>());
            HashMap<String, String> branchInfo = repositoryInfo.get(branch.getBranchName());
            branchInfo.put("Commit", branch.getPointedCommitSha1().toString());
            branchInfo.put("IsTracking", branch.getIsTracking().toString());
            branchInfo.put("IsRemote", branch.getIsRemote().toString());
            branchInfo.put("TrackingAfter", branch.getTrackingAfter() == null ? "" : branch.getTrackingAfter());
        }
        return repositoryInfo;
    }

    public void createPullRequest(MagitEngine engineOfSender, String targetBranchName,String baseBranchName,String message) throws IOException, RepositoryNotFoundException, RemoteReferenceException, UncommitedChangesException, PushException, UnhandledMergeException, ParseException, CommitNotFoundException, RemoteBranchException, PreviousCommitsLimitExceededException {
        collaborationEngine.createPullRequest(engineOfSender,targetBranchName,baseBranchName,message);
    }

    public void acceptPullRequest(int pullRequestId) throws UnhandledMergeException, MergeNotNeededException, RepositoryNotFoundException, MergeException, UncommitedChangesException, FastForwardException {
        collaborationEngine.acceptPullRequest(this, pullRequestId);
    }

    public void rejectPullRequest(int pullRequestId) throws UnhandledMergeException, MergeNotNeededException, RepositoryNotFoundException, MergeException, UncommitedChangesException, FastForwardException {
        collaborationEngine.rejectPullRequest(pullRequestId);
    }

    public CollaborationEngine getCollaborationEngine() {
        return collaborationEngine;
    }

    public ArrayList<String> getHeadBranchCommits() throws IOException {
        ArrayList<String> sha1sOfCommit = new ArrayList<>();
        LinkedList<String> sha1Queue = new LinkedList<>();
        String sha1OfActiveBranch = mBranchManager.getActiveBranch().getPointedCommitSha1().toString();
        String pathToObjectsFolder = mRepositoryManager.getRepository().getObjectsFolderPath().toString();
        sha1Queue.add(sha1OfActiveBranch);
        sha1sOfCommit.add(sha1OfActiveBranch);

        while (!sha1Queue.isEmpty()) {
            String currentCommitSha1 = sha1Queue.poll();
            String commitFileContent = FileItemHandler.zipToString(pathToObjectsFolder, currentCommitSha1);
            String previousCommitsHistory = commitFileContent.split(System.lineSeparator())[1];
            String[] separatedCommitsLine = previousCommitsHistory.split(" = ");
            if (separatedCommitsLine.length <= 1)
                continue;
            String sha1sToAdd = separatedCommitsLine[1];
            List<String> sha1List = Arrays.asList(sha1sToAdd.split(";"));
            sha1sOfCommit.addAll(sha1List);
            sha1Queue.addAll(sha1List);
        }

        return sha1sOfCommit;
    }

    public Tree getTree(String sha1) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        String pathToRepository = mRepositoryManager.getRepository().getRepositoryPath().toString();
        Path pathToCommit = Paths.get(mRepositoryManager.getRepository().getObjectsFolderPath().toString(), sha1);
        return WorkingCopyUtils.getWorkingCopyTreeFromCommit
                (Commit.createCommitInstanceByPath(pathToCommit),pathToRepository);
    }
}


