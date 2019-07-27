package com.magit.logic.system;

import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileReader;
import com.magit.logic.utils.file.FileWriter;
import com.magit.logic.utils.file.WorkingCopyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class MagitEngine {

    public MagitEngine() {
    }

    private Repository mActiveRepository;
    private Branch mActiveBranch;
    private String mUserName = "Administrator";

    public void updateUserName(String userNameToSet) {
        mUserName = userNameToSet;
    }

    public String getUserName() {
        return mUserName;
    }

    public void switchRepository(String pathOfRepository) throws RepositoryNotFoundException, IOException, ParseException {
        if (!isValidRepository(pathOfRepository))
            throw new RepositoryNotFoundException("repository Not Found");

        Path repositoryPath = Paths.get(pathOfRepository);
        loadRepository(repositoryPath);
        if (mActiveRepository.getCommitPath() != null) {
            Commit commitOfRepository = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
            if (commitOfRepository == null)
                return;
            WorkingCopyUtils workingCopyHandler = new WorkingCopyUtils(mActiveRepository.getRepositoryPath().toString(),
                    mUserName, commitOfRepository.getCreationDate());
            workingCopyHandler.clearWorkingCopyFiles(mActiveRepository.getRepositoryPath());
            workingCopyHandler.unzipWorkingCopyFromCommit(commitOfRepository, mActiveRepository.getRepositoryPath().toString());
        }
    }

    private boolean isValidRepository(String repositoryPath) {
        final String magit = ".magit";

        return Files.exists(Paths.get(repositoryPath)) &&
                Files.exists(Paths.get(repositoryPath, magit)) &&
                Files.exists(Paths.get(repositoryPath, magit, "branches", "HEAD"));
    }


    private void loadRepository(Path repositoryPath) throws IOException {
        mActiveRepository = new Repository(repositoryPath.getFileName().toString()
                , repositoryPath.getParent().toString());
        List<File> branchesFiles = (List<File>) FileUtils.listFiles(
                new File(Paths.get(repositoryPath.toString(), ".magit", "branches").toString()),
                TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

        for (File branchFile : branchesFiles) {
            if (!branchFile.getName().equals("HEAD"))
                mActiveRepository.add(branchFile.getName()
                        , new Branch(branchFile.getName(), FileReader.readFile(branchFile.getPath())));
            else {
                loadBranch(branchFile);
                mActiveRepository.add(branchFile.getName(), mActiveBranch);
            }
        }
    }

    private void loadBranch(File branchFile) throws IOException {
        String headContent = FileReader.readFile(branchFile.getPath());
        File headBranch = new File(Paths.get(branchFile.getParent(), headContent).toString());
        mActiveBranch = new Branch(headContent, FileReader.readFile(headBranch.getPath()));
    }

    public String presentCurrentCommitAndHistory() throws RepositoryNotFoundException, IOException, ParseException {
        if (!mActiveRepository.isValid())
            throw new RepositoryNotFoundException(mActiveRepository.getRepositoryName());

        Commit commit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
        WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils(mActiveRepository.getRepositoryPath().toString(), mUserName, commit.getCreationDate());
        return workingCopyUtils.getWorkingCopyContent(WorkingCopyUtils.getWorkingCopyTreeFromCommit(commit, mActiveRepository.getRepositoryPath().toString()));
    }

    public String getBranchesInfo() throws IOException {
        final String seperator = "============================================";
        StringBuilder branchesContent = new StringBuilder();

        branchesContent.append(String.format("Head Branch : %s%s%s%s",
                FileReader.readFile(mActiveRepository.getHeadPath().toString()),System.lineSeparator(),
                seperator, System.lineSeparator()));
        File branchesDirectory = new File(mActiveRepository.getBranchDirectoryPath().toString());
        File[] files = branchesDirectory.listFiles();
        if (files == null)
            return null;

        for (File branchFile: files) {
            if (!branchFile.getName().equals("HEAD"))
                branchesContent.append(String.format("%s, sha1: %s%s",
                        branchFile.getName(), FileReader.readFile(branchFile.getPath()),System.lineSeparator()));
        }

        return branchesContent.toString();
    }

    public boolean createNewBranch(String branchName)throws IOException {
        if (Files.exists(Paths.get(mActiveRepository.getBranchDirectoryPath().toString(), branchName)))
            return false;

        FileWriter.writeNewFile(
                Paths.get(mActiveRepository.getBranchDirectoryPath().toString(), branchName).toString(), "");
        return true;
    }

    public String presentCurrentBranch() throws  IOException, ParseException{
        Path pathToBranchFile = Paths.get(mActiveRepository.getBranchDirectoryPath().toString(),
                mActiveBranch.getmBranchName());
        if (Files.notExists(pathToBranchFile))
            throw new FileNotFoundException("No Branch file, repository is invalid");

        String sha1OfCommit = FileReader.readFile(pathToBranchFile.toString());
        Path pathToCommit = Paths.get(mActiveRepository.getObjectsFolderPath().toString(), sha1OfCommit);
        if (Files.notExists(pathToCommit))
            throw new FileNotFoundException("No commit file, repository is invalid");
        final String seperator = "===================================================";
        StringBuilder activeBranchHistory = new StringBuilder();
        activeBranchHistory.append(String.format("Branch Name: %s%s%s%s%s%s"
                , mActiveBranch.getmBranchName(), System.lineSeparator(),seperator, System.lineSeparator(),
                "Current Commit:", System.lineSeparator()));
        Commit mostRecentCommit = Commit.createCommitInstanceByPath(pathToCommit);
        activeBranchHistory.append(mostRecentCommit.toPrintFormat());
        for (Sha1 currentSha1 : mostRecentCommit.getLastCommitsSha1Codes()) {
            Path currentCommitPath = Paths.get(mActiveRepository.getObjectsFolderPath().toString(), currentSha1.toString());
            if (Files.notExists(currentCommitPath)) {
                throw new FileNotFoundException("Commit history is invalid, repository invalid.");
            }
            activeBranchHistory.append(String.format("%s%s", seperator, System.lineSeparator()));
            Commit currentCommitInHistory = Commit.createCommitInstanceByPath(currentCommitPath);
            activeBranchHistory.append(currentCommitInHistory.toPrintFormat());
        }

        return activeBranchHistory.toString();
    }

    public void deleteBranch(String branchNameToDelete) throws FileNotFoundException, IOException, ActiveBranchDeletedExpcetion{
        if (Files.notExists(mActiveRepository.getHeadPath()))
            throw new FileNotFoundException("Head file not found, repository is invalid.");

        String headContent = FileReader.readFile(mActiveRepository.getHeadPath().toString());
        if (branchNameToDelete.equals(headContent))
            throw new ActiveBranchDeletedExpcetion("Active Branch can't be deleted.");

        FileUtils.deleteQuietly(Paths.get(mActiveRepository.getBranchDirectoryPath().toString(), branchNameToDelete).toFile());
    }

    public void createNewRepository(String repositoryName, String fullPath) throws IllegalPathException, IOException {
        Repository repository = new Repository(repositoryName, fullPath);
        repository.create();
        mActiveRepository = repository;
        mActiveBranch = repository.getmBranches().get("master");
    }

    public void commit(String commitMessage, String creator) throws IOException, WorkingCopyIsEmptyException, ParseException, WorkingCopyStatusNotChangedComparedToLastCommitException {
        Commit commit = new Commit(commitMessage, creator, FileType.COMMIT, new Date());
        commit.generate(mActiveRepository, mActiveBranch);
    }
}


