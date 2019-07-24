package com.magit.logic.system.objects;

import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.RepositoryAlreadyExistsException;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Repository {
    private String mRepositoryName;
    private String mRepositoryParentFolderLocation;
    private HashMap<String, Branch> mBranches;

    public Repository(String mRepositoryName, String mRepositoryLocation) {
        this.mRepositoryName = mRepositoryName;
        this.mRepositoryParentFolderLocation = mRepositoryLocation;
        this.mBranches = new HashMap<>();
    }

    public void add(String key, Branch value) {
        this.mBranches.put(key, value);
    }

    public void create() throws IllegalPathException, IOException {
        Boolean validPath = false;
        File repository;
        try {
            Path filePath = Paths.get(mRepositoryParentFolderLocation, mRepositoryName, ".magit");
            repository = new File(filePath.toString());
            validPath = repository.mkdirs();
        } catch (InvalidPathException e) {
            System.out.println(e.getMessage());
            throw new IllegalPathException(e.getInput(), e.getMessage());

        }

        if (!validPath) {
            if (repository.exists())
                throw new RepositoryAlreadyExistsException(mRepositoryParentFolderLocation, mRepositoryName);
            else
                throw new IllegalPathException(mRepositoryParentFolderLocation, "wrong location");
        }

        Branch branch = new Branch("master");
        branch.create(Paths.get(mRepositoryParentFolderLocation, mRepositoryName).toString());
        mBranches.put("master", branch);
        createHeadFile("master");
    }

    private void createHeadFile(String branchName) throws IOException {
        Path path = Paths.get(mRepositoryParentFolderLocation, mRepositoryName, ".magit", "Branches", "HEAD");
        Files.createFile(path);
        BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(path.toString()));
        writer.write(branchName);
        writer.close();
    }

    public HashMap<String, Branch> getmBranches() {
        return mBranches;
    }

    public String getRepositoryName() {
        return mRepositoryName;
    }

    public String getmRepositoryParentFolderLocation() {
        return mRepositoryParentFolderLocation;
    }

    public void changeBranchPointer(String branchName, Sha1 newCommit) throws IOException {

        FileWriter.writeNewFile(Paths.get(mRepositoryParentFolderLocation, mRepositoryName,".magit","branches", branchName).toString(), newCommit.toString());
    }
}
