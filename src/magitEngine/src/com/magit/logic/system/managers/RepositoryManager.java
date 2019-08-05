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

    private final String EMPTY = "";
    public Repository getRepository() {
        return mActiveRepository;
    }

    public void setActiveRepository(Repository mActiveRepository) {
        this.mActiveRepository = mActiveRepository;
    }

    public void switchRepository(String pathOfRepository, BranchManager branchManager, String userName) throws RepositoryNotFoundException, IOException, ParseException {
        if (!isValidRepository(pathOfRepository))
            throw new RepositoryNotFoundException("Repository not found or corrupted.");

        Path repositoryPath = Paths.get(pathOfRepository);
        loadRepository(repositoryPath, branchManager);
    }

    private boolean isValidRepository(String repositoryPath) throws IOException {
        final String magit = ".magit";

        return Files.exists(Paths.get(repositoryPath)) &&
                Files.exists(Paths.get(repositoryPath, magit)) &&
                Files.exists(Paths.get(repositoryPath, magit, "branches", "HEAD")) &&
                !FileHandler.readFile(Paths.get(repositoryPath, magit, "branches", "HEAD").toString()).isEmpty() &&
                Files.exists((Paths.get(repositoryPath, magit, "REPOSITORY_NAME")));
    }

    public boolean isCommitExists(String sha1Code) throws IOException {
        return FileHandler.isContentExistsInFile(Paths.get(getRepository().getMagitFolderPath().toString(), "COMMITS").toString(), sha1Code);
    }

    private void loadRepository(Path repositoryPath, BranchManager branchManager) throws IOException {
        String repositoryName = FileHandler.readFile(Paths.get(repositoryPath.toString(), ".magit", "REPOSITORY_NAME").toString());
        mActiveRepository = new Repository(repositoryPath.toString(), repositoryName);
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

    public void unzipHeadBranchCommitWorkingCopy() throws IOException, ParseException, PreviousCommitsLimitExceededException {
        Commit commit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
        if (commit == null) return;
        WorkingCopyUtils.unzipWorkingCopyFromCommit(commit, mActiveRepository.getRepositoryPath().toString(),
                mActiveRepository.getRepositoryPath().toString());
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

    public void createNewRepository(String fullPath, BranchManager branchManager, String repositoryName) throws IllegalPathException, IOException {
        Repository repository = new Repository(fullPath, repositoryName);
        repository.create();
        mActiveRepository = repository;
        branchManager.setActiveBranch(repository.getmBranches().get("master"));
    }


    public void commit(String commitMessage, String creator, Branch mActiveBranch) throws IOException, WorkingCopyIsEmptyException, ParseException, WorkingCopyStatusNotChangedComparedToLastCommitException, PreviousCommitsLimitExceededException {
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

    public String getBranchesInfo() throws IOException, ParseException, PreviousCommitsLimitExceededException {
        final String separator = "============================================";
        StringBuilder branchesContent = new StringBuilder();
        String headBranch = FileHandler.readFile(mActiveRepository.getHeadPath().toString());
        File branchesDirectory = new File(mActiveRepository.getBranchDirectoryPath().toString());
        File[] files = branchesDirectory.listFiles();
        if (files == null)
            return null;


        for (File branchFile: files) {
            String commitSha1 = FileHandler.readFile(branchFile.getPath());
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


}
