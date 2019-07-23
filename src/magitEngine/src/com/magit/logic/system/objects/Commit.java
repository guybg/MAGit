package com.magit.logic.system.objects;

import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileZipper;
import com.magit.logic.utils.file.WorkingCopyWalker;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Commit {
    private Sha1 mWorkingCopySha1;
    private List<Sha1> mLastCommits;
    private String mCommitMessage;
    private Date mCommitDate;
    private String mCreator;
    private Boolean firstCommit = true;

    public Commit(String commitMessage, String creator) {
        mCommitMessage = commitMessage;
        mCreator = creator;
    }

    private void loadCommmit() {

    }

    public void newCommit(Repository repository, Branch branch) throws IOException {
        mCommitDate = new Date();
        if (firstCommit) {
            generateFirstCommit(mCommitMessage, mCreator, repository, branch);
        }
        repository.changeBranchPointer("master", new Sha1(getFileContent()));
    }

    private void generateFirstCommit(String commitMessage, String creator, Repository repository, Branch branch) throws IOException {
        WorkingCopyWalker workingCopyWalker = new WorkingCopyWalker(Paths.get(repository.getmRepositoryParentFolderLocation(), repository.getRepositoryName()).toString(), creator);
        mWorkingCopySha1 = workingCopyWalker.zipWorkingCopy(Paths.get(repository.getmRepositoryParentFolderLocation(), repository.getRepositoryName()).toString());

        FileZipper.zip(this, Paths.get(repository.getmRepositoryParentFolderLocation(), repository.getRepositoryName()).toString(), new Sha1(getFileContent()));

    }

    public String getFileContent() {
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        String content = "";
        content = "wc=" + System.lineSeparator() + mWorkingCopySha1;
        content += "lastCommits=" + System.lineSeparator();
        for (Sha1 commit : mLastCommits) {
            content += commit.toString();
            content += System.lineSeparator();
        }
        content += "commitMessege=" + System.lineSeparator() + mCommitMessage + dateFormat.format(mCommitDate)
                + "creator=" + System.lineSeparator() + mCreator + System.lineSeparator();

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
}
