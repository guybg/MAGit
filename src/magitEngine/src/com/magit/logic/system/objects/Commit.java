package com.magit.logic.system.objects;

import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.PreviousCommitsLimitexceededException;
import com.magit.logic.exceptions.WorkingCopyIsEmptyException;
import com.magit.logic.exceptions.WorkingCopyStatusNotChangedComparedToLastCommitException;
import com.magit.logic.system.XMLObjects.MagitRepository;
import com.magit.logic.system.XMLObjects.MagitSingleCommit;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.FileItemHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commit extends FileItem {
    private Sha1 mWorkingCopySha1;
    private Sha1 mFirstPreviousCommit;
    private Sha1 mSecondPreviousCommit;
    private String mCommitMessage;

    final String EMPTY = "";
    final String COMMITS_FILE_NAME = "COMMITS";

    public Commit(String commitMessage, String creator, FileType fileType, Date mCommitDate) {
        super(null, fileType, creator, mCommitDate, null);
        mCommitMessage = commitMessage;
        mFirstPreviousCommit = new Sha1(EMPTY, true);
        mSecondPreviousCommit = new Sha1(EMPTY, true);
    }

    private Commit(String commitMessage, String creator, FileType fileType, Date mCommitDate,
                   Sha1 sha1Code, Sha1 workingCopySha1) {
        super(null, fileType, creator, mCommitDate, null);
        mCommitMessage = commitMessage;
        mFirstPreviousCommit = new Sha1(EMPTY, true);
        mSecondPreviousCommit = new Sha1(EMPTY, true);
        mWorkingCopySha1 = workingCopySha1;
        super.mSha1Code = sha1Code;
    }

    public Commit(MagitSingleCommit singleCommit, Sha1 workingCopySha1, MagitRepository magitRepository) throws ParseException, IOException {
        super(singleCommit);
        mFirstPreviousCommit = new Sha1(EMPTY, true);
        mSecondPreviousCommit = new Sha1(EMPTY, true);
        this.mCommitMessage = singleCommit.getMessage();
        mWorkingCopySha1 = workingCopySha1;
        super.mSha1Code = new Sha1(getFileContent(), false);
        addCommitToCommitsFile(magitRepository);
    }

    public static Commit createCommitInstanceByPath(Path pathToCommit) throws IOException, ParseException, PreviousCommitsLimitexceededException {
        final int shaOfCommit1Index = 0, lastCommitsIndex = 1, commitMessageIndex = 2,
                commitDateIndex = 3, commitCreatorIndex = 4, valueOfSplit = 1, twoPartsOfSplitting = 2,
                sha1Length = 40;

        if (pathToCommit == null || Files.notExists(pathToCommit) ||
                pathToCommit.getFileName().toString().length() != sha1Length)
            return null;

        String seperator = " = ";
        Sha1 sha1Code = new Sha1(pathToCommit.getFileName().toString(), true);
        String commitContent = FileItemHandler.zipToString(pathToCommit.getParent().toString(), sha1Code);
        String[] commitLines = commitContent.split(String.format("%s", System.lineSeparator()));
        Sha1 workingCopySha1 = new Sha1(commitLines[shaOfCommit1Index].split(seperator)[valueOfSplit], true);
        String commitMessage = commitLines[commitMessageIndex].split(seperator)[valueOfSplit];
        String commitDate = commitLines[commitDateIndex].split(seperator)[valueOfSplit];
        String commitCreator = commitLines[commitCreatorIndex].split(seperator)[valueOfSplit];
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:SSS");
        Commit output = new Commit(commitMessage, commitCreator, FileType.COMMIT, dateFormat.parse(commitDate), sha1Code, workingCopySha1);
        String[] lastCommits = commitLines[lastCommitsIndex].split(seperator);
        if (lastCommits.length != twoPartsOfSplitting)
            return output;

        for (String sha1OfCommit : lastCommits[lastCommitsIndex].split(";")) {
            output.addPreviousCommitSha1(sha1OfCommit);
        }
        return output;
    }

    public void addPreceding(String contentToSha1) throws PreviousCommitsLimitexceededException {
        if (mFirstPreviousCommit.toString().equals(EMPTY)) {
            mFirstPreviousCommit = new Sha1(contentToSha1, true);
        } else if (mSecondPreviousCommit.toString().equals(EMPTY)) {
            mSecondPreviousCommit = new Sha1(contentToSha1, true);
        } else {
            throw new PreviousCommitsLimitexceededException("Wrong XML input - Theres more then two previous commits to one of the commits");
        }

    }

    public void addPreviousCommitSha1(String sha1) throws PreviousCommitsLimitexceededException {
        if (mFirstPreviousCommit.toString().equals(EMPTY)) {
            mFirstPreviousCommit = new Sha1(sha1, true);
        } else if (mSecondPreviousCommit.toString().equals(EMPTY)) {
            mSecondPreviousCommit = new Sha1(sha1, true);
        } else {
            throw new PreviousCommitsLimitexceededException(getSha1() + " commit is invalid - Theres more then two previous commits to one of the commits");
        }
    }

    public Sha1 getFirstPreviousCommit() {
        return mFirstPreviousCommit;
    }

    private String getCreator() {
        return super.mLastUpdater;
    }

    public String getSha1() {
        return super.mSha1Code.toString();
    }

    public String getCommitMessage() {
        return mCommitMessage;
    }

    public void setFirstPreviousCommit(String firstPreviousCommit) {
        this.mFirstPreviousCommit = new Sha1(firstPreviousCommit, true);
    }

    public Sha1 getSecondPreviousCommit() {
        return mSecondPreviousCommit;
    }

    public void setSecondPreviousCommit(String secondPreviousCommit) {
        this.mSecondPreviousCommit = new Sha1(secondPreviousCommit, true);
    }

    public Date getCreationDate() {
        return super.mLastModified;
    }

    public List<Sha1> getLastCommitsSha1Codes() {
        ArrayList<Sha1> lastCommitsSha1s = new ArrayList<>();
        lastCommitsSha1s.add(mFirstPreviousCommit);
        lastCommitsSha1s.add(mSecondPreviousCommit);
        return lastCommitsSha1s;
    }

    public void generateCommitFile(Path pathToObjectsFolder) throws IOException {
        File objectsFolder = new File(pathToObjectsFolder.toString());
        objectsFolder.mkdirs();
        FileItemHandler.zip(this, pathToObjectsFolder.toString(), mSha1Code);
    }

    public void generate(Repository repository, Branch branch) throws IOException, WorkingCopyIsEmptyException, ParseException, WorkingCopyStatusNotChangedComparedToLastCommitException, PreviousCommitsLimitexceededException {
        if (branch.getmPointedCommitSha1().toString().equals(EMPTY)) {
            generateFirstCommit(getCreator(), repository, branch);
            repository.changeBranchPointer(branch, new Sha1(getFileContent(), false));
        } else {
            //handle second and on commit
            WorkingCopyUtils wcw = new WorkingCopyUtils(repository.getRepositoryPath().toString(), mLastUpdater, getCreationDate());
            Tree curWc = wcw.getWc();

            Commit lastCommit = createCommitInstanceByPath(repository.getCommitPath());
            Tree oldWcFromCommit = WorkingCopyUtils.getWorkingCopyTreeFromCommit(lastCommit, repository.getRepositoryPath().toString());

            WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils(repository.getRepositoryPath().toString(), mLastUpdater, mLastModified);
            Tree fixedWc = WorkingCopyUtils.getWcWithOnlyNewchanges(curWc, oldWcFromCommit);
            mWorkingCopySha1 = fixedWc.getSha1Code();
            if (!fixedWc.getSha1Code().equals(oldWcFromCommit.getSha1Code())) {
                mFirstPreviousCommit = lastCommit.getSha1Code();
                workingCopyUtils.zipWorkingCopyFromTreeWC(fixedWc);
                super.mSha1Code = new Sha1(getFileContent(), false);
                branch.setPointedCommitSha1(super.mSha1Code);
                FileItemHandler.zip(this, Paths.get(repository.getmRepositoryLocation(), ".magit", "objects").toString(), super.mSha1Code);
                repository.changeBranchPointer(branch, new Sha1(getFileContent(), false));
            } else {
                throw new WorkingCopyStatusNotChangedComparedToLastCommitException();
            }
        }
        addCommitToCommitsFile(repository);
    }

    private void addCommitToCommitsFile(Repository repository) throws IOException {
        FileHandler.appendFileWithContentAndLine(Paths.get(repository.getMagitFolderPath().toString(), COMMITS_FILE_NAME).toString(), getSha1Code().toString());
    }

    private void addCommitToCommitsFile(MagitRepository magitRepository) throws IOException {
        FileHandler.appendFileWithContentAndLine(Paths.get(magitRepository.getLocation(), ".magit", COMMITS_FILE_NAME).toString(), getSha1Code().toString());
    }

    private void generateFirstCommit(String creator, Repository repository, Branch branch) throws IOException, WorkingCopyIsEmptyException {
        WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils(repository.getRepositoryPath().toString(), creator, mLastModified);
        mWorkingCopySha1 = workingCopyUtils.zipWorkingCopyFromCurrentWorkingCopy();
        super.mSha1Code = new Sha1(getFileContent(), false);
        branch.setPointedCommitSha1(super.mSha1Code);
        FileItemHandler.zip(this, Paths.get(repository.getRepositoryPath().toString(), ".magit", "objects").toString(), super.mSha1Code);
    }

    public String getFileContent() {
        DateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
        StringBuilder content = new StringBuilder();
        content.append(String.format("%s = %s%s%s = ",
                "wc", mWorkingCopySha1, System.lineSeparator(), "last Commits"));
        if (!mFirstPreviousCommit.toString().equals(EMPTY))
            content.append(String.format("%s%c", mFirstPreviousCommit.toString(), ';'));
        if (!mSecondPreviousCommit.toString().equals(EMPTY))
            content.append(String.format("%s%c", mSecondPreviousCommit.toString(), ';'));
        content.append(String.format("%s%s%s%s%s%s%s%s%s", System.lineSeparator(),
                "commit Messege = ", mCommitMessage, System.lineSeparator(), "commit Date = ",
                dateFormat.format(getCreationDate()), System.lineSeparator(), "creator = ",
                getCreator() + System.lineSeparator()));
        return content.toString();
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
        return "Commit{" +
                "mWorkingCopySha1 = " + mWorkingCopySha1 + '\'' +
                " LastCommits = " + mFirstPreviousCommit + ";" + mSecondPreviousCommit +
                " CommitMessage = " + mCommitMessage + '\'' +
                " LastModified = " + dateFormat.format(getCreationDate()) +
                " Creator = " + getCreator() + '\'' +
                '}';
    }

    public String toPrintFormat() {
        StringBuilder contentOfCommit = new StringBuilder();
        String linePrefix = "Commit ";
        contentOfCommit.append(String.format("%s %s [%s]%s", linePrefix, "sha1", super.mSha1Code.toString(), System.lineSeparator()));
        contentOfCommit.append(String.format("%s %s [%s]%s", linePrefix, "Message", mCommitMessage, System.lineSeparator()));
        contentOfCommit.append(String.format("%s %s [%s]%s", linePrefix, "author", getCreator(), System.lineSeparator()));
        contentOfCommit.append(String.format("%s %s [%s]%s", linePrefix, "date/time", getCreationDate().toString(), System.lineSeparator()));

        return contentOfCommit.toString();
    }

    @Override
    public Sha1 getSha1Code() {
        return super.mSha1Code;
    }

    public Sha1 getmWorkingCopySha1() {
        return mWorkingCopySha1;
    }
}
