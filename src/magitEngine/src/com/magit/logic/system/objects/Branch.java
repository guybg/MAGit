package com.magit.logic.system.objects;

import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Branch {
    private String mBranchName;
    private Sha1 mPointedCommitSha1;

    public Branch(String mBranchName) {
        this.mBranchName = mBranchName;
        mPointedCommitSha1 = new Sha1("", true);
    }

    public Branch(String branchName, String textToSha1) {
        this.mBranchName = branchName;
        mPointedCommitSha1 = new Sha1(textToSha1, true);
    }

    public Sha1 getmPointedCommitSha1() {
        return mPointedCommitSha1;
    }

    void create(String path) {
        try {
            Path filePath = Paths.get(path, ".magit", "branches", mBranchName);
            File branch = new File(filePath.toString());
            boolean newFile = !branch.exists();
            if (!newFile) {
                System.out.println("file exists");
                throw new FileAlreadyExistsException(".magit already exists");
            } else {
                FileHandler.writeNewFile(filePath.toString(), mPointedCommitSha1.toString());
                System.out.println("file created");
            }

        } catch (IllegalPathException e) {
            throw new IllegalPathException(e.getInput(), e.getMessage());
        } catch (FileAlreadyExistsException e) {
            // throw nice messege about repository already exists
        } catch (IOException e) {

            // throw io exception
        }
    }

    public String getmBranchName() {
        return mBranchName;
    }

    void setPointedCommitSha1(Sha1 mPointedCommitSha1) {
        this.mPointedCommitSha1 = mPointedCommitSha1;
    }
}
