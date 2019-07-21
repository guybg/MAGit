package com.magit.logic.system.objects;

import com.magit.logic.handle.exceptions.IllegalPathException;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Branch {
    private String mBranchName;
    private String mPointedCommitSha1;

    public Branch(String mBranchName) {
        this.mBranchName = mBranchName;
    }

    public void create(String path){
        try {
            Path filePath = Paths.get(path,".magit", "branches", mBranchName);
            File branch = new File(filePath.toString());
            branch.getParentFile().mkdirs();
            boolean newFile = branch.createNewFile();
            if (!newFile) {
                System.out.println("file exists");
                throw new FileAlreadyExistsException(".magit already exists");
            }
            else{
                System.out.println("file created");
            }

        }
        catch (IllegalPathException e){
            throw new IllegalPathException(e.getInput(), e.getMessage());
        }
        catch (FileAlreadyExistsException e){
            // throw nice messege about repository already exists
        }
        catch (IOException e){

            // throw io exception
        }
    }
}
