package com.magit.logic.system.managers;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public class BranchManager {
    private Branch mActiveBranch;

    public Branch getActiveBranch() {
        return mActiveBranch;
    }

    public void setActiveBranch(Branch branch) {
        mActiveBranch = branch;
    }

    void loadBranch(File branchFile) throws IOException {
        String headContent = FileHandler.readFile(branchFile.getPath());
        File headBranch = new File(Paths.get(branchFile.getParent(), headContent).toString());
        mActiveBranch = new Branch(headContent, FileHandler.readFile(headBranch.getPath()));
    }

    public boolean createNewBranch(String branchName, Repository repository) throws IOException {
        if (Files.exists(Paths.get(repository.toString(), branchName)))
            return false;

        repository.addBranch(branchName, new Branch(branchName, mActiveBranch.getmPointedCommitSha1().toString()));
        FileHandler.writeNewFile(Paths.get(repository.getBranchDirectoryPath().toString(), branchName).toString(), mActiveBranch.getmPointedCommitSha1().toString());
        return true;
    }

    public String presentCurrentBranch(Repository activeRepository) throws IOException, ParseException, PreviousCommitsLimitexceededException {
        Path pathToBranchFile = Paths.get(activeRepository.getBranchDirectoryPath().toString(),
                mActiveBranch.getmBranchName());
        if (Files.notExists(pathToBranchFile))
            throw new FileNotFoundException("No Branch file, repository is invalid");

        String sha1OfCommit = FileHandler.readFile(pathToBranchFile.toString());
        Path pathToCommit = Paths.get(activeRepository.getObjectsFolderPath().toString(), sha1OfCommit);
        if (Files.notExists(pathToCommit))
            throw new FileNotFoundException("No commit file, repository is invalid");
        final String seperator = "===================================================";
        StringBuilder activeBranchHistory = new StringBuilder();
        activeBranchHistory.append(String.format("Branch Name: %s%s%s%s%s%s"
                , mActiveBranch.getmBranchName(), System.lineSeparator(), seperator, System.lineSeparator(),
                "Current Commit:", System.lineSeparator()));
        Commit mostRecentCommit = Commit.createCommitInstanceByPath(pathToCommit);
        activeBranchHistory.append(mostRecentCommit.toPrintFormat());
        getAllPreviousCommitsHistoryString(mostRecentCommit, activeRepository, activeBranchHistory);
        //    for (Sha1 currentSha1 : mostRecentCommit.getLastCommitsSha1Codes()) {
        //        Path currentCommitPath = Paths.get(activeRepository.getObjectsFolderPath().toString(), currentSha1.toString());
        //        if (Files.notExists(currentCommitPath)) {
        //            throw new FileNotFoundException("Commit history is invalid, repository invalid.");
        //        }
        //        activeBranchHistory.append(String.format("%s%s", seperator, System.lineSeparator()));
        //        Commit currentCommitInHistory = Commit.createCommitInstanceByPath(currentCommitPath);
        //        activeBranchHistory.append(currentCommitInHistory.toPrintFormat());
        //    }

        return activeBranchHistory.toString();
    }

    private void getAllPreviousCommitsHistoryString(Commit mostRecentCommit, Repository activeRepository, StringBuilder activeBranchHistoryStringBuilder) throws IOException, ParseException, PreviousCommitsLimitexceededException {
        final String seperator = "===================================================";
        if (mostRecentCommit.getSha1Code().toString().equals("")) return;
        for (Sha1 currentSha1 : mostRecentCommit.getLastCommitsSha1Codes()) {
            List<Sha1> a = mostRecentCommit.getLastCommitsSha1Codes();
            if (currentSha1.toString().equals("")) return;
            Path currentCommitPath = Paths.get(activeRepository.getObjectsFolderPath().toString(), currentSha1.toString());
            if (Files.notExists(currentCommitPath)) {
                throw new FileNotFoundException("Commit history is invalid, repository invalid.");
            }
            activeBranchHistoryStringBuilder.append(String.format("%s%s", seperator, System.lineSeparator()));
            Commit currentCommitInHistory = Commit.createCommitInstanceByPath(currentCommitPath);
            if (currentCommitInHistory == null) return;
            activeBranchHistoryStringBuilder.append(currentCommitInHistory.toPrintFormat());
            getAllPreviousCommitsHistoryString(currentCommitInHistory, activeRepository, activeBranchHistoryStringBuilder);
        }
    }


    public void deleteBranch(String branchNameToDelete, Repository activeRepository) throws IOException, ActiveBranchDeletedExpcetion, BranchNotFoundException {
        if (Files.notExists(activeRepository.getHeadPath()))
            throw new FileNotFoundException("Head file not found, repository is invalid.");

        String headContent = FileHandler.readFile(activeRepository.getHeadPath().toString());
        if (branchNameToDelete.equals(headContent))
            throw new ActiveBranchDeletedExpcetion("Active Branch can't be deleted.");

        if (!activeRepository.getmBranches().containsKey(branchNameToDelete))
            throw new BranchNotFoundException(branchNameToDelete, "Branch called '" + branchNameToDelete + "' can't be deleted, because it doesn't exist at current repository.");

        FileUtils.deleteQuietly(Paths.get(activeRepository.getBranchDirectoryPath().toString(), branchNameToDelete).toFile());
    }

    public String pickHeadBranch(String wantedBranchName, Repository activeRepository,
                                 Map<FileStatus, SortedSet<Delta.DeltaFileItem>> changes) throws IOException, ParseException, BranchNotFoundException, UncommitedChangesException, PreviousCommitsLimitexceededException {
        if (Files.notExists(Paths.get(activeRepository.getBranchDirectoryPath().toString(), wantedBranchName)))
            throw new BranchNotFoundException(wantedBranchName);

        String headFileContent = FileHandler.readFile(activeRepository.getHeadPath().toString());
        if (headFileContent.equals(wantedBranchName))
            return "Wanted branch is already active.";

        if (activeRepository.areThereChanges(changes))
            throw new UncommitedChangesException("There are unsaved changes, are you sure you want to change branch without generating a commit?");


        return forcedChangeBranch(wantedBranchName, activeRepository);
    }

    public String forcedChangeBranch(String wantedBranchName, Repository activeRepository) throws IOException, ParseException, PreviousCommitsLimitexceededException {
        FileHandler.writeNewFile(activeRepository.getHeadPath().toString(), wantedBranchName);
        String wantedBranchSha1 = FileHandler.readFile(
                Paths.get(activeRepository.getBranchDirectoryPath().toString(), wantedBranchName).toString());
        Commit branchLatestCommit = Commit.createCommitInstanceByPath(
                Paths.get(activeRepository.getObjectsFolderPath().toString(), wantedBranchSha1));
        FileHandler.clearFolder(activeRepository.getRepositoryPath());

        if (branchLatestCommit != null) {
            WorkingCopyUtils.unzipWorkingCopyFromCommit(branchLatestCommit,
                    activeRepository.getRepositoryPath().toString(),
                    activeRepository.getRepositoryPath().toString());
        }
        mActiveBranch = activeRepository.getmBranches().get(wantedBranchName);
        return "Active branch has changed successfully";
    }

    public void changeBranchPointedCommit(Repository repository, Sha1 commitSha1) throws IOException, CommitNotFoundException {
        boolean commitExists = FileHandler.isContentExistsInFile(Paths.get(repository.getMagitFolderPath().toString(), "COMMITS").toString(), commitSha1.toString());
        if (!commitExists) {
            throw new CommitNotFoundException("Wrong commit sha1 code, please enter existing commit sha1 code");
        }

        repository.changeBranchPointer(mActiveBranch, commitSha1);
    }
}
