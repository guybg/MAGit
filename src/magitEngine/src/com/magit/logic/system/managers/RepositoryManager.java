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

    public void setUserName(String userName) {
        mActiveRepository.setActiveUserName(userName);
    }

    ////public String getActiveUserName(){return mActiveRepository.getActiveUserName();}
    public Repository getRepository() {
        return mActiveRepository;
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

    private void loadRepository(Path repositoryPath, BranchManager branchManager, String userName) throws IOException {
        mActiveRepository = new Repository(repositoryPath.getFileName().toString()
                , repositoryPath.getParent().toString(), userName);
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


    public String presentCurrentCommitAndHistory(String userName)
            throws RepositoryNotFoundException, IOException, ParseException, CommitNotFoundException {
        if (!mActiveRepository.isValid())
            throw new RepositoryNotFoundException(mActiveRepository.getRepositoryName());
        Commit commit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
        if (commit == null)
            throw new CommitNotFoundException("Theres no commit history to show, please add some files and commit them");

        return WorkingCopyUtils.getWorkingCopyContent(WorkingCopyUtils.getWorkingCopyTreeFromCommit(commit, mActiveRepository.getRepositoryPath().toString()), mActiveRepository.getRepositoryPath().toString(), commit.getmLastUpdater());
    }

    public void createNewRepository(String repositoryName, String fullPath, BranchManager branchManager, String userName) throws IllegalPathException, IOException {
        Repository repository = new Repository(repositoryName, fullPath, userName);
        repository.create();
        mActiveRepository = repository;
        repository.setActiveUserName(userName);
        branchManager.setActiveBranch(repository.getmBranches().get("master"));
    }


    public void commit(String commitMessage, String creator, Branch mActiveBranch) throws IOException, WorkingCopyIsEmptyException, ParseException, WorkingCopyStatusNotChangedComparedToLastCommitException {
        Commit commit = new Commit(commitMessage, creator, FileType.COMMIT, new Date());
        commit.generate(mActiveRepository, mActiveBranch);
    }

    public Map<FileStatus, SortedSet<Delta.DeltaFileItem>> checkDifferenceBetweenCurrentWCandLastCommit() throws IOException, ParseException {
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

    public String getBranchesInfo() throws IOException {
        final String seperator = "============================================";
        StringBuilder branchesContent = new StringBuilder();

        branchesContent.append(String.format("Head Branch : %s%s%s%s",
                FileHandler.readFile(mActiveRepository.getHeadPath().toString()),System.lineSeparator(),
                seperator, System.lineSeparator()));
        File branchesDirectory = new File(mActiveRepository.getBranchDirectoryPath().toString());
        File[] files = branchesDirectory.listFiles();
        if (files == null)
            return null;

        for (File branchFile: files) {
            if (!branchFile.getName().equals("HEAD"))
                branchesContent.append(String.format("%s, sha1: %s%s",
                        branchFile.getName(), FileHandler.readFile(branchFile.getPath()),System.lineSeparator()));
        }

        return branchesContent.toString();
    }


}
