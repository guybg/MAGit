package com.magit.logic.system.managers;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.objects.*;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class RepositoryManager {
    private final String EMPTY = "";
    private Repository mActiveRepository;

    public RepositoryManager() {
    }

    public RepositoryManager(Path repositoryPath, BranchManager branchManager) throws IOException {
        initializeActiveRepository(repositoryPath, branchManager);
    }

    public Repository getRepository() {
        return mActiveRepository;
    }

    public String getHeadBranch() {
        return mActiveRepository.getBranches().get("HEAD").getBranchName();
    }

    public void setActiveRepository(Repository mActiveRepository) {
        this.mActiveRepository = mActiveRepository;
    }

    public void switchRepository(String pathOfRepository, BranchManager branchManager, String userName) throws RepositoryNotFoundException, IOException, ParseException {
        if (!isValidRepository(pathOfRepository))
            throw new RepositoryNotFoundException("Repository not found or corrupted.");

        Path repositoryPath = Paths.get(pathOfRepository);
        initializeActiveRepository(repositoryPath, branchManager);
    }

    private boolean isValidRepository(String repositoryPath) throws IOException {
        final String magit = ".magit";

        return Files.exists(Paths.get(repositoryPath)) &&
                Files.exists(Paths.get(repositoryPath, magit)) &&
                Files.exists(Paths.get(repositoryPath, magit, "branches", "HEAD")) &&
                !FileHandler.readFile(Paths.get(repositoryPath, magit, "branches", "HEAD").toString()).isEmpty() &&
                Files.exists((Paths.get(repositoryPath, magit, "REPOSITORY_NAME")));
    }
    // (todo) handle load of remote branches, right now it skips them.
    public static Repository loadRepository(Path repositoryPath, BranchManager branchManager) throws IOException {

        String repositoryName = FileHandler.readFile(Paths.get(repositoryPath.toString(), ".magit", "REPOSITORY_NAME").toString());
        Repository repository = new Repository(repositoryPath.toString(), repositoryName);
        List<File> branchesFiles = (List<File>) FileUtils.listFiles(
                new File(Paths.get(repositoryPath.toString(), ".magit", "branches").toString()),
                TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        if(Files.exists(Paths.get(repositoryPath.toString(), ".magit", "REMOTE_REFERENCE"))){
            String[] remoteReferenceInfo = FileHandler.readFile(Paths.get(repositoryPath.toString(), ".magit", "REMOTE_REFERENCE").toString()).split(System.lineSeparator());
            RemoteReference remoteReference = new RemoteReference(remoteReferenceInfo[0],remoteReferenceInfo[1]);
            repository.setRemoteReference(remoteReference);
        }
        for (File branchFile : branchesFiles) {
            if (!branchFile.getName().equals("HEAD")) {
                if(!branchFile.getParentFile().getName().equals("branches")){
                    for(File branchInDirectory : Objects.requireNonNull(branchFile.getParentFile().listFiles())){
                        loadBranchFromFile(branchInDirectory,repository, true);
                    }
                }else {
                    loadBranchFromFile(branchFile, repository, false);
                }
            }
        }
        for (File branchFile : branchesFiles) {
            if (branchFile.getName().equals("HEAD")){
                branchManager.setActiveBranch(repository.getBranches().get(FileHandler.readFile(branchFile.getPath())));
                repository.addBranch(branchFile.getName(), branchManager.getActiveBranch());
            }
        }
        return repository;
    }

    private static void loadBranchFromFile(File branchFile, Repository repository, Boolean isBranchInDirectory) throws IOException {
        final String sha1 = "sha1", isRemote = "isRemote", isTracking = "isTracking", trackingAfter = "trackingAfter";

        String trackingAfterValue = null;
        HashMap<String,String> branchContent = Repository.readBranchContent(branchFile);
        if(!branchContent.get(trackingAfter).equals("null")){
            trackingAfterValue = branchContent.get(trackingAfter);
        }
        String branchName = branchFile.getName();
        if(isBranchInDirectory){
            branchName = String.join("\\",branchFile.getParentFile().getName(),branchName);
        }
        repository.addBranch(branchName
                , new Branch(branchName, branchContent.get(sha1),trackingAfterValue, Boolean.valueOf(branchContent.get(isRemote)), Boolean.valueOf(branchContent.get(isTracking))));
    }


    private void initializeActiveRepository(Path repositoryPath, BranchManager branchManager) throws IOException {

        mActiveRepository = loadRepository(repositoryPath, branchManager);
    }

    public void unzipHeadBranchCommitWorkingCopy() throws IOException, ParseException, PreviousCommitsLimitExceededException {
        Commit commit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
        if (commit == null) return;
        WorkingCopyUtils.unzipWorkingCopyFromCommit(commit, mActiveRepository.getRepositoryPath().toString(),
                mActiveRepository.getRepositoryPath().toString());
    }

    public static void unzipHeadBranchCommitWorkingCopy(Repository repository) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        Commit commit = Commit.createCommitInstanceByPath(repository.getCommitPath());
        if (commit == null) return;
        WorkingCopyUtils.unzipWorkingCopyFromCommit(commit, repository.getRepositoryPath().toString(),
                repository.getRepositoryPath().toString());
    }

    public String presentCurrentCommitAndHistory()
            throws RepositoryNotFoundException, IOException, ParseException, CommitNotFoundException, PreviousCommitsLimitExceededException {
        if (!mActiveRepository.isValid())
            throw new RepositoryNotFoundException("Repository at location " + mActiveRepository.getRepositoryPath().toString() + " is corrupted.");

        Commit commit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
        if (commit == null)
            throw new CommitNotFoundException("There's no commit history to show, please add some files and commit them");

        return WorkingCopyUtils.getWorkingCopyContent(WorkingCopyUtils.getWorkingCopyTreeFromCommit(commit, mActiveRepository.getRepositoryPath().toString()), mActiveRepository.getRepositoryPath().toString(), commit.getLastUpdater());
    }

    public ObservableList<FileItemInfo> guiPresentCurrentCommitAndHistory()
              throws RepositoryNotFoundException, IOException, ParseException, CommitNotFoundException, PreviousCommitsLimitExceededException {
            if (!mActiveRepository.isValid())
                throw new RepositoryNotFoundException("Repository at location " + mActiveRepository.getRepositoryPath().toString() + " is corrupted.");

            Commit commit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
            if (commit == null)
                throw new CommitNotFoundException("There's no commit history to show, please add some files and commit them");

            return WorkingCopyUtils.guiGetWorkingCopyContent(WorkingCopyUtils.getWorkingCopyTreeFromCommit(commit, mActiveRepository.getRepositoryPath().toString()), mActiveRepository.getRepositoryPath().toString(), commit.getLastUpdater());
    }

    public void createNewRepository(String fullPath, BranchManager branchManager, String repositoryName) throws IllegalPathException, IOException, RepositoryAlreadyExistsException {
        if(isValidRepository(fullPath))
            throw new RepositoryAlreadyExistsException(fullPath);
        Repository repository = new Repository(fullPath, repositoryName);
        repository.create();
        mActiveRepository = repository;
        branchManager.setActiveBranch(repository.getBranches().get("master"));
    }

    public void commit(String commitMessage, String creator, Branch mActiveBranch) throws IOException, WorkingCopyIsEmptyException, ParseException, WorkingCopyStatusNotChangedComparedToLastCommitException, PreviousCommitsLimitExceededException, FastForwardException {
        Commit commit = new Commit(commitMessage, creator, FileType.COMMIT, new Date());
        commit.generate(mActiveRepository, mActiveBranch);
    }

    public Map<FileStatus, SortedSet<Delta.DeltaFileItem>> checkDifferenceBetweenCurrentWCAndLastCommit() throws IOException, ParseException, PreviousCommitsLimitExceededException {
        WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils(mActiveRepository.getRepositoryPath().toString(),
                EMPTY, new Date());
        SortedSet<Delta.DeltaFileItem> curWcDeltaFiles;
        SortedSet<Delta.DeltaFileItem> commitDeltaFiles;
        curWcDeltaFiles = workingCopyUtils.getAllDeltaFilesFromCurrentWc();
        Commit lastCommit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
        commitDeltaFiles = WorkingCopyUtils.getDeltaFileItemSetFromCommit(lastCommit, mActiveRepository.getRepositoryPath().toString());
        if (commitDeltaFiles == null) commitDeltaFiles = new TreeSet<>();
        return WorkingCopyUtils.getDifferencesBetweenCurrentWcAndLastCommit(curWcDeltaFiles, commitDeltaFiles);
    }

    public Map<FileStatus, SortedSet<Delta.DeltaFileItem>> checkDifferencesBetweenTwoCommits(String sha1OfFirstCommit, String sha1OfSecondCommit) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        SortedSet<Delta.DeltaFileItem> firstCommitDeltaFiles;
        SortedSet<Delta.DeltaFileItem> secondCommitDeltaFiles;
        Commit firstCommit = Commit.createCommitInstanceByPath(Paths.get(mActiveRepository.getObjectsFolderPath().toString(), sha1OfFirstCommit));
        Commit secondCommit = Commit.createCommitInstanceByPath(Paths.get(mActiveRepository.getObjectsFolderPath().toString(), sha1OfSecondCommit));
        firstCommitDeltaFiles = WorkingCopyUtils.getDeltaFileItemSetFromCommit(firstCommit, mActiveRepository.getRepositoryPath().toString());
        secondCommitDeltaFiles = WorkingCopyUtils.getDeltaFileItemSetFromCommit(secondCommit, mActiveRepository.getRepositoryPath().toString());
        if (firstCommitDeltaFiles == null) firstCommitDeltaFiles = new TreeSet<>();
        if (secondCommitDeltaFiles == null) secondCommitDeltaFiles = new TreeSet<>();
        return WorkingCopyUtils.getDifferencesBetweenCurrentWcAndLastCommit(firstCommitDeltaFiles, secondCommitDeltaFiles);
    }

    public String getBranchesInfo() throws IOException, ParseException, PreviousCommitsLimitExceededException {
        final String separator = "============================================";
        StringBuilder branchesContent = new StringBuilder();
        String headBranch = FileHandler.readFile(mActiveRepository.getHeadPath().toString());
        File branchesDirectory = new File(mActiveRepository.getBranchDirectoryPath().toString());
        File[] files = branchesDirectory.listFiles();
        if (files == null)
            return null;


        for (File branchFile : files) {
            String commitSha1 = Repository.readBranchContent(branchFile).get("sha1");
            String commitMessage = "none";
            if (commitSha1.isEmpty()) commitSha1 = "none";
            if (!branchFile.getName().equals("HEAD")) {
                Commit commit = Commit.createCommitInstanceByPath(Paths.get(mActiveRepository.getObjectsFolderPath().toString(), commitSha1));
                if (commit != null) {
                    commitMessage = commit.getCommitMessage();
                }
                branchesContent.append(String.format("Branch name: %s%s%s", branchFile.getName().equals(headBranch) ? "[HEAD] " : EMPTY, branchFile.getName(), System.lineSeparator()));
                branchesContent.append(String.format("Commit Sha1: %s%s", commitSha1, System.lineSeparator()));
                branchesContent.append(String.format("Commit Message: %s%s", commitMessage, System.lineSeparator()));
                branchesContent.append(String.format("%s%s", separator, System.lineSeparator()));
            }
        }
        return branchesContent.toString();
    }


    public String getWorkingCopyStatus(String userName) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        StringBuilder workingCopyStatusContent = new StringBuilder();
        workingCopyStatusContent.append(String.format("Repository name: %s%s", mActiveRepository.getRepositoryName(), System.lineSeparator()));
        workingCopyStatusContent.append(String.format("Repository location: %s%s", mActiveRepository.getRepositoryPath(), System.lineSeparator()));
        workingCopyStatusContent.append(String.format("Active user: %s%s", userName, System.lineSeparator()));
        workingCopyStatusContent.append(String.format("%s", System.lineSeparator()));

        Map<FileStatus, SortedSet<Delta.DeltaFileItem>> differences = checkDifferenceBetweenCurrentWCAndLastCommit();
        if (differences.values().stream().allMatch(Set::isEmpty))
            return workingCopyStatusContent.append(String.format("%s%s", "There are no open changes.", System.lineSeparator())).toString();
        workingCopyStatusContent.append(String.format("New Files: %s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("==========%s", System.lineSeparator()));
        if (differences.get(FileStatus.NEW).isEmpty())
            workingCopyStatusContent.append("-NONE-" + System.lineSeparator());
        for (Delta.DeltaFileItem item : differences.get(FileStatus.NEW)) {
            workingCopyStatusContent.append(String.format("(+) %s%s", item.getFullPath(), System.lineSeparator()));
        }
        workingCopyStatusContent.append(String.format("%s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("Edited Files: %s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("==========%s", System.lineSeparator()));
        if (differences.get(FileStatus.EDITED).isEmpty())
            workingCopyStatusContent.append("-NONE-" + System.lineSeparator());
        for (Delta.DeltaFileItem item : differences.get(FileStatus.EDITED)) {
            workingCopyStatusContent.append(String.format("%s%s", item.getFullPath(), System.lineSeparator()));
        }
        workingCopyStatusContent.append(String.format("%s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("Deleted Files: %s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("==========%s", System.lineSeparator()));
        if (differences.get(FileStatus.REMOVED).isEmpty())
            workingCopyStatusContent.append("-NONE-" + System.lineSeparator());
        for (Delta.DeltaFileItem item : differences.get(FileStatus.REMOVED)) {
            workingCopyStatusContent.append(String.format("(-) %s%s", item.getFullPath(), System.lineSeparator()));
        }
        workingCopyStatusContent.append(String.format("%s", System.lineSeparator()));
        return workingCopyStatusContent.toString();
    }


    public ObservableList<Branch> getBranches() {
        return FXCollections.observableArrayList(mActiveRepository.getBranches()
                .entrySet().stream().filter(e -> !e.getKey()
                        .equals("HEAD")).map(Map.Entry::getValue).collect(Collectors.toList()));
    }

    public ArrayList<String> guiGetRepositoryCommitList() throws IOException {
           return new ArrayList<>(Arrays.asList(mActiveRepository.getAllCommitsOfRepository()));
    }

    public String guiGetBranchInfo(Branch branch) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        String sha1OfCommit = branch.getPointedCommitSha1().toString();
        Path pathToCommit = Paths.get(mActiveRepository.getObjectsFolderPath().toString(), sha1OfCommit);
        Commit commitOfBranch = Commit.createCommitInstanceByPath(pathToCommit);
        String commitMessage = commitOfBranch == null ? "" : commitOfBranch.getCommitMessage();
        String info =  String.format("Branch Name: %s%sSha1: %s%sCommit Message:%s%sRemote: %s",
                branch.getBranchName(), System.lineSeparator(), branch.getPointedCommitSha1().toString(), System.lineSeparator(),
                commitMessage, System.lineSeparator(),branch.getIsRemote());
        if(branch.getIsTracking()){
            info = info.concat(String.format("%sTracking after: %s", System.lineSeparator(),branch.getTrackingAfter()));
        }
        return info;
    }

    public boolean hasRemoteReference(){
        if(mActiveRepository.getRemoteReference() == null){
            return false;
        }
        return true;
    }
}
