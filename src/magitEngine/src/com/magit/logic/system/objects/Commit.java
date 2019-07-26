package com.magit.logic.system.objects;

import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.WorkingCopyIsEmptyException;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileZipper;
import com.magit.logic.utils.file.WorkingCopyUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class Commit extends FileItem{
    private Sha1 mWorkingCopySha1;
    private LinkedList<Sha1> mLastCommits;
    private String mCommitMessage;
    private Boolean firstCommit = true;
    private Sha1 mCommitSha1Code;

    public Commit(String commitMessage, String creator, FileType fileType, Date mCommitDate) {
        super(null, fileType, creator, mCommitDate);
        mCommitMessage = commitMessage;
        mLastCommits = new LinkedList<>();
    }

    private Commit(String commitMessage, String creator,
                  FileType fileType, Date mCommitDate, Sha1 sha1Code, Sha1 workingCopySha1){
        super(null, fileType, creator, mCommitDate);
        mCommitMessage = commitMessage;
        mLastCommits = new LinkedList<>();
        mCommitSha1Code = sha1Code;
        mWorkingCopySha1 = workingCopySha1;
    }

    private String getCreator() {
        return super.mLastUpdater;
    }

    public static Commit createCommitInstanceByPath(Path pathToCommit) throws IOException, ParseException {
        String seperator = " = ";
        Sha1 sha1Code = new Sha1(pathToCommit.getFileName().toString(), true);
        String commitContent = FileZipper.zipToString(pathToCommit.getParent().toString(), sha1Code);
        String[] commitLines = commitContent.split(String.format("%s",System.lineSeparator()));
        Sha1 workingCopySha1 = new Sha1(commitLines[0].split(seperator)[1], true);
        String commitMessage = commitLines[2].split(seperator)[1];
        String commitDate = commitLines[3].split(seperator)[1];
        String commitCreator = commitLines[4].split(seperator)[1];
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        return new Commit(commitMessage, commitCreator,
                FileType.COMMIT, dateFormat.parse(commitDate), sha1Code, workingCopySha1);
    }

    public Date getCreationDate() {
        return super.mLastModified;
    }

    //String commitMessage, String creator, FileType fileType, Date mCommitDate
    public void generate(Repository repository, Branch branch) throws IOException, WorkingCopyIsEmptyException, ParseException {
        if (firstCommit == false) {
            generateFirstCommit(mCommitMessage, getCreator(), repository, branch);
        }
        /*
            handle second and on commit
         */
        WorkingCopyUtils wcw = new WorkingCopyUtils(repository.getRepositoryPath().toString(), mLastUpdater, getCreationDate());
        Tree curWc = wcw.getWc();
        Commit lastCommit = createCommitInstanceByPath(Paths.get(repository.getObjectsFolderPath().toString(), mLastCommits.getLast().toString()));
        Tree wcToCommit = WorkingCopyUtils.getWorkingCopyTreeFromCommit(lastCommit, repository.getRepositoryPath().toString());

        WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils(Paths.get(repository.getmRepositoryParentFolderLocation(), repository.getRepositoryName()).toString(), mLastUpdater, mLastModified);
        Tree wc = WorkingCopyUtils.getWcWithOnlyNewchanges(wcToCommit, curWc);
        mWorkingCopySha1 = wc.getSha1Code();
        mLastCommits.addAll(lastCommit.mLastCommits);
        //zip wc from tree <--------------TODO TODO TODO
        mCommitSha1Code = new Sha1(getFileContent(), false);
        repository.changeBranchPointer(branch.getmBranchName(), new Sha1(getFileContent(), false));
    }

    private void generateFirstCommit(String commitMessage, String creator, Repository repository, Branch branch) throws IOException, WorkingCopyIsEmptyException {
        WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils(Paths.get(repository.getmRepositoryParentFolderLocation(), repository.getRepositoryName()).toString(), creator, mLastModified);
        mWorkingCopySha1 = workingCopyUtils.zipWorkingCopy();
        mCommitSha1Code = new Sha1(getFileContent(), false);
        FileZipper.zip(this, Paths.get(repository.getmRepositoryParentFolderLocation(), repository.getRepositoryName(), ".magit", "objects").toString(), mCommitSha1Code);
    }

    public String getFileContent() {
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        StringBuilder content = new StringBuilder();
        content.append(String.format("%s = %s%s%s = ",
                "wc", mWorkingCopySha1, System.lineSeparator(), "lastCommits"));
        for (Sha1 commit : mLastCommits) {
            content.append(String.format("%s%c",commit.toString(),';'));
        }
        content.append(String.format("%s%s%s%s%s%s%s%s%s", System.lineSeparator(),
                "commitMessege = ", mCommitMessage, System.lineSeparator(), "commitDate = " ,
                dateFormat.format(getCreationDate()), System.lineSeparator(), "creator = " ,
                getCreator() + System.lineSeparator()));
        return content.toString();
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        return "Commit{" +
                "mWorkingCopySha1='" + mWorkingCopySha1 + '\'' +
                ", mLastCommits=" + mLastCommits +
                ", mCommitMessage='" + mCommitMessage + '\'' +
                ", mLastModified=" + dateFormat.format(getCreationDate()) +
                ", mCreator='" + getCreator() + '\'' +
                ", firstCommit=" + firstCommit +
                '}';
    }

    public String toPrintFormat() {
        StringBuilder contentOfCommit = new StringBuilder();
        String linePrefix = "Commit ";
        contentOfCommit.append(String.format("%s %s [%s]%s", linePrefix, "sha1", mCommitSha1Code.toString(), System.lineSeparator()));
        contentOfCommit.append(String.format("%s %s [%s]%s", linePrefix, "Message", mCommitMessage, System.lineSeparator()));
        contentOfCommit.append(String.format("%s %s [%s]%s", linePrefix, "author", getCreator(), System.lineSeparator()));
        contentOfCommit.append(String.format("%s %s [%s]%s", linePrefix, "date/time", getCreationDate().toString(), System.lineSeparator()));
        contentOfCommit.append(System.lineSeparator());

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
