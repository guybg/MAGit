package com.magit.logic.system.objects;

import com.magit.logic.utils.digest.Sha1;

import java.text.SimpleDateFormat;
import java.util.List;

public class Commit {
    private String mWorkingCopySha1;
    private List<Sha1> mLastCommits;
    private String mCommitMessage;
    private SimpleDateFormat mCommitDate;
    private String mCreator;
    private Boolean firstCommit = true;

    public Commit(Repository repository, Branch branch) {
        loadCommmit();
    }

    private void loadCommmit() {
    }

    public void newCommit(String commitMessage, String creator) {
        if (firstCommit) {
            generateFirstCommit(commitMessage, creator);
        }
    }

    private void generateFirstCommit(String commitMessage, String creator) {

    }

}
