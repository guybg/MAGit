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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commit extends FileItem{
    private Sha1 mWorkingCopySha1;
    private List<Sha1> mLastCommits;
    private String mCommitMessage;
    private Date mCommitDate;
    private String mCreator;
    private Boolean firstCommit = true;
    private Sha1 mSha1Code;

    public Commit(String commitMessage, String creator, FileType fileType, Date mCommitDate) {
        super(null, fileType,creator,mCommitDate);
        mCommitMessage = commitMessage;
        mCreator = creator;
        mLastCommits = new ArrayList<>();
    }

    public static Commit parseCommitContent(Path pathToCommit) throws IOException, ParseException {
        String commitContent = FileZipper.zipToString(pathToCommit.getParent().toString(), new Sha1(pathToCommit.getFileName().toString(), true));
        String[] commitLines = commitContent.split(String.format("%s",System.lineSeparator()));
        String commitMessage = commitLines[2].split("=")[1];
        String commitDate = commitLines[3].split("=")[1];
        String commitCreator = commitLines[4].split("=")[1];
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        return new Commit(commitMessage, commitCreator, FileType.COMMIT, dateFormat.parse(commitDate));
    }

    public Date getDate() {
        return mCommitDate;
    }

    public void newCommit(Repository repository, Branch branch) throws IOException, WorkingCopyIsEmptyException {
        mCommitDate = new Date();
        if (firstCommit) {
            generateFirstCommit(mCommitMessage, mCreator, repository, branch);
        }
        /*
            handle second and on commit
         */



        repository.changeBranchPointer(branch.getmBranchName(), new Sha1(getFileContent(), false));
    }

    private void generateFirstCommit(String commitMessage, String creator, Repository repository, Branch branch) throws IOException, WorkingCopyIsEmptyException {
        WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils(Paths.get(repository.getmRepositoryParentFolderLocation(), repository.getRepositoryName()).toString(), creator, mCommitDate);
        mWorkingCopySha1 = workingCopyUtils.zipWorkingCopy(Paths.get(repository.getmRepositoryParentFolderLocation(), repository.getRepositoryName()).toString());
        mSha1Code = new Sha1(getFileContent(), false);
        FileZipper.zip(this, Paths.get(repository.getmRepositoryParentFolderLocation(), repository.getRepositoryName(), ".magit", "objects").toString(), mSha1Code);
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
                dateFormat.format(mCommitDate), System.lineSeparator(), "creator = " ,
                mCreator + System.lineSeparator()));
        return content.toString();
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        return "Commit{" +
                "mWorkingCopySha1='" + mWorkingCopySha1 + '\'' +
                ", mLastCommits=" + mLastCommits +
                ", mCommitMessage='" + mCommitMessage + '\'' +
                ", mCommitDate=" + dateFormat.format(mCommitDate) +
                ", mCreator='" + mCreator + '\'' +
                ", firstCommit=" + firstCommit +
                '}';
    }

    @Override
    public Sha1 getSha1Code() {
        return mSha1Code;
    }

    public Sha1 getmWorkingCopySha1() {
        return mWorkingCopySha1;
    }
}
