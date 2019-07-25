package com.magit.logic.system.objects;

import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.WorkingCopyIsEmptyException;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileZipper;
import com.magit.logic.utils.file.WorkingCopyUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
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

    private void loadCommmit() {

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
        String content = "";
        content = "wc=" + System.lineSeparator() + mWorkingCopySha1+ System.lineSeparator();
        content += "lastCommits=" + System.lineSeparator();
        for (Sha1 commit : mLastCommits) {
            content += commit.toString();
            content += System.lineSeparator();
        }
        content += "commitMessege=" + System.lineSeparator() + mCommitMessage + System.lineSeparator()
                + "commitDate" + System.lineSeparator() + dateFormat.format(mCommitDate)
                +System.lineSeparator() + "creator=" + System.lineSeparator() + mCreator + System.lineSeparator();

        return content;
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
