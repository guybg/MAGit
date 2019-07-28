package com.magit.logic.system.objects;

import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.WorkingCopyIsEmptyException;
import com.magit.logic.exceptions.WorkingCopyStatusNotChangedComparedToLastCommitException;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.FileItemHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class Commit extends FileItem {
    private Sha1 mWorkingCopySha1;
    private LinkedList<Sha1> mLastCommits;
    private String mCommitMessage;
    private Sha1 mCommitSha1Code;

    public Commit(String commitMessage, String creator, FileType fileType, Date mCommitDate) {
        super(null, fileType, creator, mCommitDate, null);
        mCommitMessage = commitMessage;
        mLastCommits = new LinkedList<>();
    }

    private Commit(String commitMessage, String creator,
                   FileType fileType, Date mCommitDate, Sha1 sha1Code, Sha1 workingCopySha1) {
        super(null, fileType, creator, mCommitDate, null);
        mCommitMessage = commitMessage;
        mLastCommits = new LinkedList<>();
        super.mSha1Code = sha1Code;
        mCommitSha1Code = sha1Code;
        mWorkingCopySha1 = workingCopySha1;
    }

    private String getCreator() {
        return super.mLastUpdater;
    }

    public LinkedList<Sha1> getLastCommitsSha1Codes() {
        return mLastCommits;
    }

    public static Commit createCommitInstanceByPath(Path pathToCommit) throws IOException, ParseException {
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
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        Commit output = new Commit(commitMessage, commitCreator, FileType.COMMIT, dateFormat.parse(commitDate), sha1Code, workingCopySha1);
        String[] lastCommits = commitLines[lastCommitsIndex].split(seperator);
        if (lastCommits.length != twoPartsOfSplitting)
            return output;

        for (String sha1OfCommit : lastCommits[lastCommitsIndex].split(";")) {
            output.mLastCommits.add(new Sha1(sha1OfCommit, true));
        }
        return output;
    }

    public Date getCreationDate() {
        return super.mLastModified;
    }

    public void generate(Repository repository, Branch branch) throws IOException, WorkingCopyIsEmptyException, ParseException, WorkingCopyStatusNotChangedComparedToLastCommitException {
        if (branch.getmPointedCommitSha1().toString().equals("")) {
            generateFirstCommit(getCreator(), repository, branch);
            repository.changeBranchPointer(branch.getmBranchName(), new Sha1(getFileContent(), false));
        } else {
        //handle second and on commit
            WorkingCopyUtils wcw = new WorkingCopyUtils(repository.getRepositoryPath().toString(), mLastUpdater, getCreationDate());
            Tree curWc = wcw.getWc();

            Commit lastCommit = createCommitInstanceByPath(repository.getCommitPath());
            Tree oldWcFromCommit = WorkingCopyUtils.getWorkingCopyTreeFromCommit(lastCommit, repository.getRepositoryPath().toString());

            WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils(Paths.get(repository.getmRepositoryParentFolderLocation(), repository.getRepositoryName()).toString(), mLastUpdater, mLastModified);
            Tree fixedWc = WorkingCopyUtils.getWcWithOnlyNewchanges(curWc, oldWcFromCommit);
            mWorkingCopySha1 = fixedWc.getSha1Code();
            if (!fixedWc.getSha1Code().equals(oldWcFromCommit.getSha1Code())) {
                mLastCommits.addAll(lastCommit.mLastCommits);
                mLastCommits.add(lastCommit.getSha1Code());
                workingCopyUtils.zipWorkingCopyFromTreeWC(fixedWc);
                mCommitSha1Code = new Sha1(getFileContent(), false);
                branch.setPointedCommitSha1(mCommitSha1Code);
                FileItemHandler.zip(this, Paths.get(repository.getmRepositoryParentFolderLocation(), repository.getRepositoryName(), ".magit", "objects").toString(), mCommitSha1Code);
                repository.changeBranchPointer(branch.getmBranchName(), new Sha1(getFileContent(), false));
            } else {
                throw new WorkingCopyStatusNotChangedComparedToLastCommitException();
            }
        }
    }

    private void generateFirstCommit(String creator, Repository repository, Branch branch) throws IOException, WorkingCopyIsEmptyException {
        WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils(Paths.get(repository.getmRepositoryParentFolderLocation(), repository.getRepositoryName()).toString(), creator, mLastModified);
        mWorkingCopySha1 = workingCopyUtils.zipWorkingCopyFromCurrentWorkingCopy();
        mCommitSha1Code = new Sha1(getFileContent(), false);
        branch.setPointedCommitSha1(mCommitSha1Code);
        FileItemHandler.zip(this, Paths.get(repository.getmRepositoryParentFolderLocation(), repository.getRepositoryName(), ".magit", "objects").toString(), mCommitSha1Code);
    }

    public String getFileContent() {
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        StringBuilder content = new StringBuilder();
        content.append(String.format("%s = %s%s%s = ",
                "wc", mWorkingCopySha1, System.lineSeparator(), "last Commits"));
        for (Sha1 commit : mLastCommits) {
            content.append(String.format("%s%c", commit.toString(), ';'));
        }
        content.append(String.format("%s%s%s%s%s%s%s%s%s", System.lineSeparator(),
                "commit Messege = ", mCommitMessage, System.lineSeparator(), "commit Date = ",
                dateFormat.format(getCreationDate()), System.lineSeparator(), "creator = ",
                getCreator() + System.lineSeparator()));
        return content.toString();
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        return "Commit{" +
                "mWorkingCopySha1 = " + mWorkingCopySha1 + '\'' +
                " LastCommits = " + mLastCommits +
                " CommitMessage = " + mCommitMessage + '\'' +
                " LastModified = " + dateFormat.format(getCreationDate()) +
                " Creator = " + getCreator() + '\'' +
                '}';
    }

    public String toPrintFormat() {
        StringBuilder contentOfCommit = new StringBuilder();
        String linePrefix = "Commit ";
        contentOfCommit.append(String.format("%s %s [%s]%s", linePrefix, "sha1", mCommitSha1Code.toString(), System.lineSeparator()));
        contentOfCommit.append(String.format("%s %s [%s]%s", linePrefix, "Message", mCommitMessage, System.lineSeparator()));
        contentOfCommit.append(String.format("%s %s [%s]%s", linePrefix, "author", getCreator(), System.lineSeparator()));
        contentOfCommit.append(String.format("%s %s [%s]%s", linePrefix, "date/time", getCreationDate().toString(), System.lineSeparator()));

        return contentOfCommit.toString();
    }

    @Override
    public Sha1 getSha1Code() {
        return mCommitSha1Code;
    }

    public Sha1 getmWorkingCopySha1() {
        return mWorkingCopySha1;
    }
}
