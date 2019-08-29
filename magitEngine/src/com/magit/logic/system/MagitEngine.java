package com.magit.logic.system;

import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.Model;
import com.magit.controllers.BranchesHistoryScreenController;
import com.magit.controllers.MainScreenController;
import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.managers.BranchManager;
import com.magit.logic.system.managers.CollaborationEngine;
import com.magit.logic.system.managers.MergeEngine;
import com.magit.logic.system.managers.RepositoryManager;
import com.magit.logic.system.objects.*;
import com.magit.logic.system.tasks.CollectFileItemsInfoTask;
import com.magit.logic.system.tasks.NewCommitTask;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.utils.compare.Delta.DeltaFileItem;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import com.magit.logic.visual.node.CommitNode;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import puk.team.course.magit.ancestor.finder.AncestorFinder;
import puk.team.course.magit.ancestor.finder.CommitRepresentative;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class MagitEngine {

    private final String BLANK_SPACE = " \t\u00A0\u1680\u180e\u2000\u200a\u202f\u205f\u3000\u2800";
    private final RepositoryManager mRepositoryManager;
    private final BranchManager mBranchManager;
    private String mUserName = "Administrator";
    private MergeEngine mergeEngine;


    public MagitEngine() {
        mRepositoryManager = new RepositoryManager();
        mBranchManager = new BranchManager();
        mergeEngine = new MergeEngine();
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

    public void loadHeadBranchCommitFiles(String path, boolean forceCreation) throws JAXBException, IOException, ParseException, PreviousCommitsLimitExceededException, XmlFileException, IllegalPathException, RepositoryAlreadyExistsException {
        //RepositoryXmlParser parser = new RepositoryXmlParser();
        //Repository repository = parser.parseXMLToRepository(path, mBranchManager, mUserName, forceCreation);
        //mRepositoryManager.setActiveRepository(repository);
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
            throw new UncommitedChangesException("There are unsaved changes compared to current commit.");
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

    public void cloneTest(){
        CollaborationEngine collaborationEngine = new CollaborationEngine();
        try {
            collaborationEngine.cloneRepository("c:/repo2","d:/repoCloned",new BranchManager(),"test");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalPathException e) {
            e.printStackTrace();
        }
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

    public void createNewBranch(String branchName) throws IOException, RepositoryNotFoundException, InvalidNameException, BranchAlreadyExistsException {
        repositoryNotFoundCheck();
        mBranchManager.createNewBranch(branchName, mRepositoryManager.getRepository(),false,false,null);
    }

    public void createNewBranch(String branchName, String sha1OfCommit) throws IOException, RepositoryNotFoundException, InvalidNameException, BranchAlreadyExistsException {
        repositoryNotFoundCheck();
        mBranchManager.createNewBranch(branchName, mRepositoryManager.getRepository(),sha1OfCommit,false,false,null);
    }

    public void deleteBranch(String branchNameToDelete) throws IOException, ActiveBranchDeletedException, RepositoryNotFoundException, BranchNotFoundException {
        repositoryNotFoundCheck();
        mBranchManager.deleteBranch(branchNameToDelete, mRepositoryManager.getRepository());
    }

    public String pickHeadBranch(String branchName) throws IOException, ParseException, RepositoryNotFoundException, BranchNotFoundException, UncommitedChangesException, PreviousCommitsLimitExceededException, InvalidNameException {
        repositoryNotFoundCheck();
        if (StringUtils.containsAny(branchName, BLANK_SPACE) || branchName.isEmpty())
            throw new InvalidNameException("Branch name cannot be empty or with whitespace.");
        return mBranchManager.pickHeadBranch(branchName,
                mRepositoryManager.getRepository(), mRepositoryManager.checkDifferenceBetweenCurrentWCAndLastCommit());
    }

    public String presentCurrentBranch() throws IOException, ParseException, RepositoryNotFoundException, PreviousCommitsLimitExceededException {
        repositoryNotFoundCheck();
        return mBranchManager.presentCurrentBranch(mRepositoryManager.getRepository());
    }

    public TreeSet<CommitNode> guiBranchesHistory(Model model, BranchesHistoryScreenController branchesHistoryScreenController) throws ParseException, PreviousCommitsLimitExceededException, IOException {
       return mBranchManager.guiPresentBranchesHistory(mRepositoryManager.getRepository(),model, branchesHistoryScreenController);
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

    public void merge(String branchName) throws UnhandledMergeException, MergeNotNeededException, FastForwardException {
        try {
            //if(mRepositoryManager.headBranchHasUnhandledMerge())
            //    throw new UnhandledMergeException("Unhandled merge already exists, please solve conflicts and commit open changes");
            mergeEngine.merge(mRepositoryManager.getRepository(), mRepositoryManager.getRepository().getBranches().get(branchName));
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (PreviousCommitsLimitExceededException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

    public void updateSolvedConflict(String path, String fileName, String fileContent){
        mergeEngine.saveSolvedConflictItem(path,fileName,fileContent,mRepositoryManager.getRepository());
    }

    public String getMergedWithBranchNameFromUnhandledMerge() throws IOException {
        String content = FileHandler.readFile(Paths.get(mRepositoryManager.getRepository().getMagitFolderPath().toString()
                ,".merge", mBranchManager.getActiveBranch().getBranchName(),"merge-info").toString());
        return content.split(System.lineSeparator())[3];

    }

    public void activeBranchHasUnhandeledMerge() throws UnhandledMergeException {
        if(mRepositoryManager.getRepository().headBranchHasUnhandledMerge())
            throw new UnhandledMergeException("");
    }
}


