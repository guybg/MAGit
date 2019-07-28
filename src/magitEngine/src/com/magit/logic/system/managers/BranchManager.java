package com.magit.logic.system.managers;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.ActiveBranchDeletedExpcetion;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

public class BranchManager {
    private Branch mActiveBranch;

    public Branch getActiveBranch() {
        return mActiveBranch;
    }

    public void loadBranch(File branchFile) throws IOException {
        String headContent = FileHandler.readFile(branchFile.getPath());
        File headBranch = new File(Paths.get(branchFile.getParent(), headContent).toString());
        mActiveBranch = new Branch(headContent, FileHandler.readFile(headBranch.getPath()));
    }



    public boolean createNewBranch(String branchName, Repository repository)throws IOException {
        if (Files.exists(Paths.get(repository.toString(), branchName)))
            return false;

        FileHandler.writeNewFile( Paths.get(repository.getBranchDirectoryPath().toString(), branchName).toString(), mActiveBranch.getmPointedCommitSha1().toString());
        return true;
    }

    public String presentCurrentBranch(Repository activeRepository) throws  IOException, ParseException {
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
                , mActiveBranch.getmBranchName(), System.lineSeparator(),seperator, System.lineSeparator(),
                "Current Commit:", System.lineSeparator()));
        Commit mostRecentCommit = Commit.createCommitInstanceByPath(pathToCommit);
        activeBranchHistory.append(mostRecentCommit.toPrintFormat());
        for (Sha1 currentSha1 : mostRecentCommit.getLastCommitsSha1Codes()) {
            Path currentCommitPath = Paths.get(activeRepository.getObjectsFolderPath().toString(), currentSha1.toString());
            if (Files.notExists(currentCommitPath)) {
                throw new FileNotFoundException("Commit history is invalid, repository invalid.");
            }
            activeBranchHistory.append(String.format("%s%s", seperator, System.lineSeparator()));
            Commit currentCommitInHistory = Commit.createCommitInstanceByPath(currentCommitPath);
            activeBranchHistory.append(currentCommitInHistory.toPrintFormat());
        }

        return activeBranchHistory.toString();
    }

    public void deleteBranch(String branchNameToDelete, Repository activeRepository) throws IOException, ActiveBranchDeletedExpcetion {
        if (Files.notExists(activeRepository.getHeadPath()))
            throw new FileNotFoundException("Head file not found, repository is invalid.");

        String headContent = FileHandler.readFile(activeRepository.getHeadPath().toString());
        if (branchNameToDelete.equals(headContent))
            throw new ActiveBranchDeletedExpcetion("Active Branch can't be deleted.");

        FileUtils.deleteQuietly(Paths.get(activeRepository.getBranchDirectoryPath().toString(), branchNameToDelete).toFile());
    }

    public String pickHeadBranch(String wantedBranchName, Repository activeRepository,
                                 MultiValuedMap<FileStatus, String> changes) throws IOException, ParseException {
        if (Files.notExists(Paths.get(activeRepository.getBranchDirectoryPath().toString(), wantedBranchName)))
            throw new FileNotFoundException("Branch doesn't exist");

        if (activeRepository.areThereChanges(changes))
            return "There are unsaved changes, branch can't be switched.";

        String headFileContent = FileHandler.readFile(activeRepository.getHeadPath().toString());
        if (headFileContent.equals(wantedBranchName))
            return "Wanted branch is already active.";

        FileHandler.writeNewFile(activeRepository.getHeadPath().toString(), wantedBranchName);
        String wantedBranchSha1 = FileHandler.readFile(
                Paths.get(activeRepository.getBranchDirectoryPath().toString(), wantedBranchName).toString());
        Commit branchLatestCommit = Commit.createCommitInstanceByPath(
                Paths.get(activeRepository.getObjectsFolderPath().toString(), wantedBranchSha1));
        FileHandler.clearFolder(activeRepository.getRepositoryPath());

        if (branchLatestCommit != null) {
            WorkingCopyUtils wcCopyUtils = new WorkingCopyUtils(activeRepository.getRepositoryPath().toString(),
                    activeRepository.getUpdaterName(), branchLatestCommit.getCreationDate());
            wcCopyUtils.unzipWorkingCopyFromCommit(branchLatestCommit, activeRepository.getRepositoryPath().toString());
        }
        mActiveBranch = activeRepository.getmBranches().get(wantedBranchName);
        return "Active branch has changed successfully";
    }
}
