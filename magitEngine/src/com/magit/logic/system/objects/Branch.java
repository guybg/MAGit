package com.magit.logic.system.objects;

import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Branch {
    private final String mBranchName;
    private Sha1 mPointedCommitSha1;

    private String mTrackingAfter = null;
    private Boolean mIsRemote = false;
    private Boolean mTracking = false;

    Branch(String mBranchName) {
        this.mBranchName = mBranchName;
        final String EMPTY = "";
        mPointedCommitSha1 = new Sha1(EMPTY, true);
    }

    public Branch(String branchName, String textToSha1) {
        this.mBranchName = branchName;
        mPointedCommitSha1 = new Sha1(textToSha1, true);
    }

    public void setIsRemote(boolean mIsRemote) {
        this.mIsRemote = mIsRemote;
    }

    public void setTrackingAfter(String mTrackingAfter) {
        this.mTrackingAfter = mTrackingAfter;
    }

    public void setIsTracking(Boolean mTracking) {
        this.mTracking = mTracking;
    }

    public String getTrackingAfter() {
        return mTrackingAfter;
    }

    public Boolean getIsRemote() {
        return mIsRemote;
    }

    public Boolean getIsTracking() {
        return mTracking;
    }

    public Branch(String branchName, String textToSha1, String trackingAfter, boolean isRemote, boolean tracking) {
        this.mBranchName = branchName;
        mPointedCommitSha1 = new Sha1(textToSha1, true);
        mTrackingAfter = trackingAfter;
        mIsRemote = isRemote;
        mTracking = tracking;
    }

    public Sha1 getPointedCommitSha1() {
        return mPointedCommitSha1;
    }

    void setPointedCommitSha1(Sha1 mPointedCommitSha1) {
        this.mPointedCommitSha1 = mPointedCommitSha1;
    }

    void create(String path) throws IllegalPathException, IOException {
        try {
            Path filePath = Paths.get(path, ".magit", "branches", mBranchName);
            File branch = new File(filePath.toString());
            boolean newFile = !branch.exists();
            if (!newFile) {
                //System.out.println("file exists"); TODO(REMOVE WHEN SUBMITTING)
                throw new FileAlreadyExistsException(".magit already exists");
            } else {
                FileHandler.appendFileWithContentAndLine(filePath.toString(), mPointedCommitSha1.toString());
                FileHandler.appendFileWithContentAndLine(filePath.toString(),mIsRemote.toString());
                FileHandler.appendFileWithContentAndLine(filePath.toString(),mTracking.toString());
                FileHandler.appendFileWithContentAndLine(filePath.toString(),mTrackingAfter);
                //System.out.println("file created"); TODO(REMOVE WHEN SUBMITTING)
            }

        } catch (InvalidPathException e) {
            throw new IllegalPathException(path + " is not a valid path.");
        }
    }

    public String getBranchName() {
        return mBranchName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return Objects.equals(mBranchName, branch.mBranchName) &&
                Objects.equals(mPointedCommitSha1, branch.mPointedCommitSha1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mBranchName, mPointedCommitSha1);
    }
}
