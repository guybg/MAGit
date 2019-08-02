package com.magit.logic.system.managers;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;

public class RepositoryManager {
    private Repository mActiveRepository;

    public Repository getRepository() {
        return mActiveRepository;
    }

    public void setmActiveRepository(Repository mActiveRepository) {
        this.mActiveRepository = mActiveRepository;
    }

    public void switchRepository(String pathOfRepository, BranchManager branchManager, String userName) throws RepositoryNotFoundException, IOException, ParseException {
        if (!isValidRepository(pathOfRepository))
            throw new RepositoryNotFoundException("repository Not Found");

        Path repositoryPath = Paths.get(pathOfRepository);
        loadRepository(repositoryPath, branchManager, userName);
    }

    private boolean isValidRepository(String repositoryPath) {
        final String magit = ".magit";

        return Files.exists(Paths.get(repositoryPath)) &&
                Files.exists(Paths.get(repositoryPath, magit)) &&
                Files.exists(Paths.get(repositoryPath, magit, "branches", "HEAD"));
    }

    private void loadRepository(Path repositoryPath, BranchManager branchManager, String userName) throws IOException, ParseException {
        String repositoryName = FileHandler.readFile(Paths.get(repositoryPath.toString(), ".magit", "REPOSITORY_NAME").toString());
        mActiveRepository = new Repository(repositoryPath.toString(), userName, repositoryName);
        List<File> branchesFiles = (List<File>) FileUtils.listFiles(
                new File(Paths.get(repositoryPath.toString(), ".magit", "branches").toString()),
                TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

        for (File branchFile : branchesFiles) {
            if (!branchFile.getName().equals("HEAD"))
                mActiveRepository.addBranch(branchFile.getName()
                        , new Branch(branchFile.getName(), FileHandler.readFile(branchFile.getPath())));
            else {
                branchManager.loadBranch(branchFile);
                mActiveRepository.addBranch(branchFile.getName(), branchManager.getActiveBranch());
            }
        }
    }

    public void unzipHeadBranchCommitWorkingCopy() throws IOException, ParseException, PreviousCommitsLimitexceededException {
        Commit commit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
        WorkingCopyUtils.unzipWorkingCopyFromCommit(commit, mActiveRepository.getRepositoryPath().toString(),
                mActiveRepository.getRepositoryPath().toString());
    }

    public String presentCurrentCommitAndHistory(String userName)
            throws RepositoryNotFoundException, IOException, ParseException, CommitNotFoundException, PreviousCommitsLimitexceededException {
        if (!mActiveRepository.isValid())
            throw new RepositoryNotFoundException(mActiveRepository.getRepositoryPath().toString());

        Commit commit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
        if (commit == null)
            throw new CommitNotFoundException("Theres no commit history to show, please add some files and commit them");

        return WorkingCopyUtils.getWorkingCopyContent(WorkingCopyUtils.getWorkingCopyTreeFromCommit(commit, mActiveRepository.getRepositoryPath().toString()), mActiveRepository.getRepositoryPath().toString(), commit.getmLastUpdater());
    }

    public void createNewRepository(String fullPath, BranchManager branchManager, String userName, String repositoryName) throws IllegalPathException, IOException {
        Repository repository = new Repository(fullPath, userName, repositoryName);
        repository.create();
        mActiveRepository = repository;
        branchManager.setActiveBranch(repository.getmBranches().get("master"));
    }


    public void commit(String commitMessage, String creator, Branch mActiveBranch) throws IOException, WorkingCopyIsEmptyException, ParseException, WorkingCopyStatusNotChangedComparedToLastCommitException, PreviousCommitsLimitexceededException {
        Commit commit = new Commit(commitMessage, creator, FileType.COMMIT, new Date());
        commit.generate(mActiveRepository, mActiveBranch);
    }

    public Map<FileStatus, SortedSet<Delta.DeltaFileItem>> checkDifferenceBetweenCurrentWCandLastCommit() throws IOException, ParseException, PreviousCommitsLimitexceededException {
        WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils(mActiveRepository.getRepositoryPath().toString(),
                "", new Date());
        SortedSet<Delta.DeltaFileItem> curWcDeltaFiles;
        SortedSet<Delta.DeltaFileItem> commitDeltaFiles;
        curWcDeltaFiles = workingCopyUtils.getAllDeltaFilesFromCurrentWc();
        Commit lastCommit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
        commitDeltaFiles = WorkingCopyUtils.getDeltaFileItemSetFromCommit(lastCommit, mActiveRepository.getRepositoryPath().toString());
        if (commitDeltaFiles == null) commitDeltaFiles = new TreeSet<>();
        return WorkingCopyUtils.getDifferencesBetweenCurrentWcAndLastCommit(curWcDeltaFiles, commitDeltaFiles);
    }

    public String getBranchesInfo() throws IOException, ParseException, PreviousCommitsLimitexceededException {
        final String seperator = "============================================";
        StringBuilder branchesContent = new StringBuilder();
        String headBranch = FileHandler.readFile(mActiveRepository.getHeadPath().toString());
        File branchesDirectory = new File(mActiveRepository.getBranchDirectoryPath().toString());
        File[] files = branchesDirectory.listFiles();
        if (files == null)
            return null;


        for (File branchFile: files) {
            String commitSha1 = FileHandler.readFile(branchFile.getPath());
            String commitMessage = "none";
            if (!branchFile.getName().equals("HEAD")) {
                Commit commit = Commit.createCommitInstanceByPath(Paths.get(mActiveRepository.getObjectsFolderPath().toString(), commitSha1));
                if (commit != null) {
                    commitMessage = commit.getCommitMessage();
                }
                branchesContent.append(String.format("Branch name: %s%s%s", branchFile.getName().equals(headBranch) ? "[HEAD] " : "", branchFile.getName(), System.lineSeparator()));
                branchesContent.append(String.format("Commit Sha1: %s%s", commitSha1, System.lineSeparator()));
                branchesContent.append(String.format("Commit Message: %s%s", commitMessage, System.lineSeparator()));
                branchesContent.append(String.format("%s%s", seperator, System.lineSeparator()));
            }
        }
        return branchesContent.toString();
    }

    public String getWorkingCopyStatus(String userName) throws IOException, ParseException, PreviousCommitsLimitexceededException {
        final String seperator = "============================================";
        StringBuilder workingCopyStatusContent = new StringBuilder();
        workingCopyStatusContent.append(String.format("Repository name: %s%s", mActiveRepository.getRepositoryName(), System.lineSeparator()));
        workingCopyStatusContent.append(String.format("Repository location: %s%s", mActiveRepository.getRepositoryPath(), System.lineSeparator()));
        workingCopyStatusContent.append(String.format("Active user: %s%s", userName, System.lineSeparator()));

        Map<FileStatus, SortedSet<Delta.DeltaFileItem>> differences = checkDifferenceBetweenCurrentWCandLastCommit();
        workingCopyStatusContent.append(String.format("New Items: %s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("==========%s", System.lineSeparator()));
        for (Delta.DeltaFileItem item : differences.get(FileStatus.NEW)) {
            workingCopyStatusContent.append(String.format("%s%s", item.getFullPath(), System.lineSeparator()));
        }
        workingCopyStatusContent.append(String.format("%s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("Edited Items: %s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("==========%s", System.lineSeparator()));
        for (Delta.DeltaFileItem item : differences.get(FileStatus.EDITED)) {
            workingCopyStatusContent.append(String.format("%s%s", item.getFullPath(), System.lineSeparator()));
        }
        workingCopyStatusContent.append(String.format("%s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("Deleted Items: %s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("==========%s", System.lineSeparator()));
        for (Delta.DeltaFileItem item : differences.get(FileStatus.REMOVED)) {
            workingCopyStatusContent.append(String.format("%s%s", item.getFullPath(), System.lineSeparator()));
        }
        workingCopyStatusContent.append(String.format("%s", System.lineSeparator()));
        return workingCopyStatusContent.toString();
    }


}
