package com.magit.logic.system;

import com.magit.logic.exceptions.*;
import com.magit.logic.system.managers.BranchManager;
import com.magit.logic.system.managers.RepositoryManager;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
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
        mRepositoryManager.setUserName(userNameToSet);
    }

    public void readRepositoryDetailsFromXML(String path)throws JAXBException, FileNotFoundException {
    }

    public void switchRepository(String pathOfRepository) throws IOException, ParseException, RepositoryNotFoundException {
        mRepositoryManager.switchRepository(pathOfRepository, mBranchManager);
    }

    public String presentCurrentCommitAndHistory() throws IOException, ParseException, RepositoryNotFoundException {
        return mRepositoryManager.presentCurrentCommitAndHistory();
    }

    public void checkDifferenceBetweenCurrentWCandLastCommit() throws IOException, ParseException {
        mRepositoryManager.checkDifferenceBetweenCurrentWCandLastCommit();
    }

    public void commit(String inputFromUser) throws IOException, WorkingCopyIsEmptyException, ParseException,
            WorkingCopyStatusNotChangedComparedToLastCommitException { mRepositoryManager.commit(inputFromUser,
                mRepositoryManager.getRepository().getUpdaterName(), mBranchManager.getActiveBranch());
    }

    public String getBranchesInfo()throws IOException {
        return mRepositoryManager.getBranchesInfo();
    }

    public void createNewRepository(Path pathToFile) throws IOException {
        mRepositoryManager.createNewRepository(pathToFile.getFileName().toString(),
                pathToFile.getParent().toString(), null);
    }

    public boolean createNewBranch(String branchName) throws IOException {
        return mBranchManager.createNewBranch(branchName, mRepositoryManager.getRepository());
    }

    public void deleteBranch(String branchNameToDelete) throws IOException, ActiveBranchDeletedExpcetion {
        mBranchManager.deleteBranch(branchNameToDelete, mRepositoryManager.getRepository());
    }

    public String pickHeadBranch(String branchName) throws IOException, ParseException {
        return mBranchManager.pickHeadBranch(branchName,
                mRepositoryManager.getRepository(), mRepositoryManager.checkDifferenceBetweenCurrentWCandLastCommit());
    }

    public String presentCurrentBranch() throws IOException, ParseException{
        return mBranchManager.presentCurrentBranch(mRepositoryManager.getRepository());
    }
}


