package com.magit.logic.system;

import com.magit.logic.exceptions.*;
import com.magit.logic.system.managers.BranchManager;
import com.magit.logic.system.managers.RepositoryManager;
import com.magit.logic.system.managers.RepositoryXmlParser;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.NoSuchElementException;

public class MagitEngine {

    private final String BLANK_SPACE = " \t\u00A0\u1680\u180e\u2000\u200a\u202f\u205f\u3000\u2800";
    private final RepositoryManager mRepositoryManager;
    private final BranchManager mBranchManager;
    private String mUserName = "Administrator";

    public MagitEngine() {
        mRepositoryManager = new RepositoryManager();
        mBranchManager = new BranchManager();
    }

    public void updateUserName(String userNameToSet) throws InvalidNameException {
        if (StringUtils.containsOnly(userNameToSet, BLANK_SPACE) || userNameToSet.isEmpty())
            throw new InvalidNameException("Username should contain at least one alphanumeric character from A–Z or 0–9 or any symbol that is not a blank space");
        mUserName = userNameToSet;
    }

    public void repositoryNotFoundCheck() throws RepositoryNotFoundException {
        if (mRepositoryManager.getRepository() == null)
            throw new RepositoryNotFoundException("Please load or create a repository before trying this operation");
    }

    public void loadRepositoryFromXML(String path, boolean forceCreation) throws JAXBException, IOException, ParseException, PreviousCommitsLimitExceededException, XmlFileException, IllegalPathException, RepositoryAlreadyExistsException {
        RepositoryXmlParser parser = new RepositoryXmlParser();
        Repository repository = parser.parseXMLToRepository(path, mBranchManager, mUserName, forceCreation);
        mRepositoryManager.setActiveRepository(repository);
        mRepositoryManager.unzipHeadBranchCommitWorkingCopy();
    }

    public void exportRepositoryToXML(String path, String fileName) throws IOException, ParseException, PreviousCommitsLimitExceededException
            , JAXBException, IllegalPathException {
        try {
            Path fullPath = Paths.get(path, fileName.concat(".xml"));
            FileHandler.writeNewFolder(Paths.get(path).toString());
            RepositoryXmlParser parser = new RepositoryXmlParser();
            parser.writeRepositoryToXML(mRepositoryManager.getRepository(), fullPath.toAbsolutePath().toString());
        } catch (IllegalArgumentException | IOException e) {
            throw new IllegalPathException("Invalid file path: " + e.getMessage());
        }
    }

    public void switchRepository(String pathOfRepository) throws IOException, ParseException, RepositoryNotFoundException {
        mRepositoryManager.switchRepository(Paths.get(pathOfRepository).toAbsolutePath().toString(), mBranchManager, mUserName);

    }

    public String presentCurrentCommitAndHistory() throws IOException, ParseException, RepositoryNotFoundException, CommitNotFoundException, PreviousCommitsLimitExceededException {
        repositoryNotFoundCheck();
        return mRepositoryManager.presentCurrentCommitAndHistory();
    }

    public String changeBranchPointedCommit(String commitSha1) throws IOException, CommitNotFoundException, ParseException, RepositoryNotFoundException, PreviousCommitsLimitExceededException {
        repositoryNotFoundCheck();
        mBranchManager.changeBranchPointedCommit(mRepositoryManager.getRepository(), new Sha1(commitSha1, true));
        FileHandler.clearFolder(mRepositoryManager.getRepository().getRepositoryPath());
        mRepositoryManager.unzipHeadBranchCommitWorkingCopy();
        return mRepositoryManager.presentCurrentCommitAndHistory();
    }

    public void workingCopyChangedComparedToCommit() throws ParseException, PreviousCommitsLimitExceededException, IOException, RepositoryNotFoundException, UncommitedChangesException {
        repositoryNotFoundCheck();
        if (mRepositoryManager.getRepository().areThereChanges(mRepositoryManager.checkDifferenceBetweenCurrentWCAndLastCommit()))
            throw new UncommitedChangesException("There are unsaved changes compared to current commit.");
    }

    public void commit(String inputFromUser) throws IOException, WorkingCopyIsEmptyException, ParseException, RepositoryNotFoundException,
            WorkingCopyStatusNotChangedComparedToLastCommitException, PreviousCommitsLimitExceededException {
        repositoryNotFoundCheck();
        mRepositoryManager.commit(inputFromUser, mUserName, mBranchManager.getActiveBranch());
    }

    public String getBranchesInfo() throws IOException, RepositoryNotFoundException, ParseException, PreviousCommitsLimitExceededException {
        repositoryNotFoundCheck();
        return mRepositoryManager.getBranchesInfo();
    }

    public String getUserName() {
        return mUserName;
    }

    public void createNewRepository(Path pathToFile, String repositoryName) throws IllegalPathException, InvalidNameException, RepositoryAlreadyExistsException {
        try {
            if (StringUtils.containsOnly(repositoryName, BLANK_SPACE) || repositoryName.isEmpty())
                throw new InvalidNameException("Repository name should contain at least one alphanumeric character from A–Z or 0–9 or any symbol that is not a blank space");
            mRepositoryManager.createNewRepository(pathToFile.toString(), mBranchManager, repositoryName);
        } catch (NullPointerException | InvalidPathException | IOException e) {
            throw new IllegalPathException(pathToFile.toString() + " is not a valid path.");
        }
    }

    public void createNewBranch(String branchName) throws IOException, RepositoryNotFoundException, InvalidNameException, BranchAlreadyExistsException {
        repositoryNotFoundCheck();
        mBranchManager.createNewBranch(branchName, mRepositoryManager.getRepository());
    }

    public void deleteBranch(String branchNameToDelete) throws IOException, ActiveBranchDeletedException, RepositoryNotFoundException, BranchNotFoundException {
        repositoryNotFoundCheck();
        mBranchManager.deleteBranch(branchNameToDelete, mRepositoryManager.getRepository());
    }

    public String pickHeadBranch(String branchName) throws IOException, ParseException, RepositoryNotFoundException, BranchNotFoundException, UncommitedChangesException, PreviousCommitsLimitExceededException, InvalidNameException {
        repositoryNotFoundCheck();
        if (StringUtils.containsAny(branchName, BLANK_SPACE) || branchName.isEmpty())
            throw new InvalidNameException("Branch name cannot be empty or with whitespace.");
        return mBranchManager.pickHeadBranch(branchName,
                mRepositoryManager.getRepository(), mRepositoryManager.checkDifferenceBetweenCurrentWCAndLastCommit());
    }

    public String presentCurrentBranch() throws IOException, ParseException, RepositoryNotFoundException, PreviousCommitsLimitExceededException {
        repositoryNotFoundCheck();
        return mBranchManager.presentCurrentBranch(mRepositoryManager.getRepository());
    }

    public String getWorkingCopyStatus() throws IOException, ParseException, RepositoryNotFoundException, PreviousCommitsLimitExceededException {
        repositoryNotFoundCheck();
        return mRepositoryManager.getWorkingCopyStatus(mUserName);
    }

    public String forcedChangeBranch(String branchName) throws ParseException, IOException, PreviousCommitsLimitExceededException {
        return mBranchManager.forcedChangeBranch(branchName,
                mRepositoryManager.getRepository());
    }
}

