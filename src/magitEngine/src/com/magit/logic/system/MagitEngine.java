package com.magit.logic.system;

import com.magit.logic.exceptions.*;
import com.magit.logic.system.managers.BranchManager;
import com.magit.logic.system.managers.RepositoryManager;
import com.magit.logic.system.managers.RepositoryXmlParser;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;
import org.apache.commons.io.FileUtils;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;

public class MagitEngine {

    public MagitEngine() {
        mRepositoryManager = new RepositoryManager();
        mBranchManager = new BranchManager();
    }
    private RepositoryManager mRepositoryManager;
    private BranchManager mBranchManager;

    private String mUserName = "Administrator";

    public void updateUserName(String userNameToSet) {
        mUserName = userNameToSet;
        ////mRepositoryManager.setUserName(userNameToSet);

    }

    private void repositoryNotFoundCheck() throws RepositoryNotFoundException {
        if (mRepositoryManager.getRepository() == null)
            throw new RepositoryNotFoundException("Please load or create a repository before trying this operation");
    }

    public void loadRepositoryFromXML(String path) throws JAXBException, IOException, ParseException, PreviousCommitsLimitexceededException, XmlFileException {
        RepositoryXmlParser parser = new RepositoryXmlParser();
        Repository repository = parser.parseXMLToRepository(path, mBranchManager, mUserName);
        mRepositoryManager.setmActiveRepository(repository);
        mRepositoryManager.unzipHeadBranchCommitWorkingCopy();
    }

    public void switchRepository(String pathOfRepository) throws IOException, ParseException, RepositoryNotFoundException {
        mRepositoryManager.switchRepository(pathOfRepository, mBranchManager, mUserName);
    }

    public String presentCurrentCommitAndHistory() throws IOException, ParseException, RepositoryNotFoundException, CommitNotFoundException, PreviousCommitsLimitexceededException {
        repositoryNotFoundCheck();
        return mRepositoryManager.presentCurrentCommitAndHistory(mUserName);
    }

    public void checkDifferenceBetweenCurrentWCandLastCommit() throws IOException, ParseException, RepositoryNotFoundException, PreviousCommitsLimitexceededException {
        repositoryNotFoundCheck();
        mRepositoryManager.checkDifferenceBetweenCurrentWCandLastCommit();
    }

    public String changeBranchPointedCommit(String commitSha1) throws IOException, CommitNotFoundException, ParseException, RepositoryNotFoundException, PreviousCommitsLimitexceededException {
        repositoryNotFoundCheck();
        mBranchManager.changeBranchPointedCommit(mRepositoryManager.getRepository(), new Sha1(commitSha1, true));
        FileHandler.clearFolder(mRepositoryManager.getRepository().getRepositoryPath());
        mRepositoryManager.unzipHeadBranchCommitWorkingCopy();
        return mRepositoryManager.presentCurrentCommitAndHistory(mUserName);
    }

    public void workingCopyChangedComparedToCommit() throws ParseException, PreviousCommitsLimitexceededException, IOException, RepositoryNotFoundException, UncommitedChangesException {
        repositoryNotFoundCheck();
        if (mRepositoryManager.getRepository().areThereChanges(mRepositoryManager.checkDifferenceBetweenCurrentWCandLastCommit()))
            throw new UncommitedChangesException("There are unsaved changes compared to current commit.");
    }
    public void commit(String inputFromUser) throws IOException, WorkingCopyIsEmptyException, ParseException, RepositoryNotFoundException,
            WorkingCopyStatusNotChangedComparedToLastCommitException, PreviousCommitsLimitexceededException {
        repositoryNotFoundCheck();
        mRepositoryManager.commit(inputFromUser, mUserName, mBranchManager.getActiveBranch());
    }

    public String getBranchesInfo() throws IOException, RepositoryNotFoundException, ParseException, PreviousCommitsLimitexceededException {
        repositoryNotFoundCheck();
        return mRepositoryManager.getBranchesInfo();
    }

    public void createNewRepository(Path pathToFile, String repositoryName) throws IOException {
        try {
            mRepositoryManager.createNewRepository(pathToFile.toString(), mBranchManager, mUserName, repositoryName);
        } catch (NullPointerException e) {
            throw new IllegalPathException(pathToFile.toString(), "isn't a legal path");
        }
    }

    public boolean createNewBranch(String branchName) throws IOException, RepositoryNotFoundException {
        repositoryNotFoundCheck();
        return mBranchManager.createNewBranch(branchName, mRepositoryManager.getRepository());
    }

    public void deleteBranch(String branchNameToDelete) throws IOException, ActiveBranchDeletedExpcetion, RepositoryNotFoundException, BranchNotFoundException {
        repositoryNotFoundCheck();
        mBranchManager.deleteBranch(branchNameToDelete, mRepositoryManager.getRepository());
    }

    public String pickHeadBranch(String branchName) throws IOException, ParseException, RepositoryNotFoundException, BranchNotFoundException, UncommitedChangesException, PreviousCommitsLimitexceededException {
        repositoryNotFoundCheck();
        return mBranchManager.pickHeadBranch(branchName,
                mRepositoryManager.getRepository(), mRepositoryManager.checkDifferenceBetweenCurrentWCandLastCommit());
    }

    public String presentCurrentBranch() throws IOException, ParseException, RepositoryNotFoundException, PreviousCommitsLimitexceededException {
        repositoryNotFoundCheck();
        return mBranchManager.presentCurrentBranch(mRepositoryManager.getRepository());
    }

    public String getWorkingCopyStatus() throws IOException, ParseException, RepositoryNotFoundException, PreviousCommitsLimitexceededException {
        repositoryNotFoundCheck();
        return mRepositoryManager.getWorkingCopyStatus(mUserName);
    }

    public String forcedChangeBranch(String branchName) throws ParseException, IOException, PreviousCommitsLimitexceededException {
        return mBranchManager.forcedChangeBranch(branchName,
                mRepositoryManager.getRepository());
    }
}


