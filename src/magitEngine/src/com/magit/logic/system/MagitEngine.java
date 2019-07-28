package com.magit.logic.system;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.managers.BranchManager;
import com.magit.logic.system.managers.RepositoryManager;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.system.objects.Tree;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
            WorkingCopyStatusNotChangedComparedToLastCommitException {
        mRepositoryManager.commit(inputFromUser,
                mRepositoryManager.getRepository().getUpdaterName(), mBranchManager.getActiveBranch());
    }

    public String getBranchesInfo()throws IOException {
        return mRepositoryManager.getBranchesInfo();
    }

    public void createNewRepository(Path pathToFile) throws IOException {
        mRepositoryManager.createNewRepository(pathToFile.getFileName().toString(),
                pathToFile.getParent().toString(), mBranchManager.getActiveBranch());
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


