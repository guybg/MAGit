package com.magit.logic.system.objects;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.RepositoryAlreadyExistsException;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

public class Repository {

    private final String BRANCHES = "branches";
    private String mActiveUser;
    private String mRepositoryName;
    private String mRepositoryParentFolderLocation;
    private HashMap<String, Branch> mBranches;
    private Path pathToRepository;
    private Path pathToMagit;
    private Path pathToHead;

    public Repository(String mRepositoryName, String mRepositoryLocation, String mUserName) {
        this.mRepositoryName = mRepositoryName;
        this.mRepositoryParentFolderLocation = mRepositoryLocation;
        this.mBranches = new HashMap<>();
        this.pathToRepository = Paths.get(mRepositoryParentFolderLocation , mRepositoryName);
        this.pathToMagit = Paths.get(pathToRepository.toString(), ".magit");
        this.pathToHead = Paths.get(pathToMagit.toString(), BRANCHES, "HEAD");
        this.mActiveUser = mUserName;
    }

    public void addBranch(String key, Branch value) {
        this.mBranches.put(key, value);
    }

    public void setActiveUserName(String name) {
        mActiveUser = name;
    }

    // public String getActiveUserName(){
    //     return mActiveUser;
    //}

    private Path getBranchPath(String branchName) {
        return Paths.get(pathToMagit.toString(), BRANCHES, branchName);
    }

    public Path getRepositoryPath() {
        return Paths.get(mRepositoryParentFolderLocation, mRepositoryName);
    }

    public Path getHeadPath() {
        return pathToHead;
    }

    public Path getBranchDirectoryPath() {
        return Paths.get(pathToMagit.toString(), BRANCHES);
    }

    public boolean isValid() {
        return Files.exists(Paths.get(mRepositoryParentFolderLocation)) &&
                Files.exists(pathToRepository) && Files.exists(pathToMagit) && Files.exists(pathToHead)
                && Files.exists(Paths.get(pathToMagit.toString(), BRANCHES, "master"));
    }

    public Path getCommitPath()throws IOException {

        String branchName = FileHandler.readFile(pathToHead.toString());
        Path pathToBranchFile = getBranchPath(branchName);
        String branchFileContent = FileHandler.readFile(pathToBranchFile.toString());
        if (branchFileContent.equals(""))
            return null;

        if (Files.notExists(pathToBranchFile))
            return null;
        String sha1OfCommit = FileHandler.readFile(pathToBranchFile.toString());
        return Paths.get(pathToMagit.toString(), "objects", sha1OfCommit);
    }

    public Path getObjectsFolderPath(){
        return Paths.get(pathToMagit.toString(), "objects");
    }

    public void create() throws IllegalPathException, IOException {
        boolean validPath;
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
        createHeadFile();
    }

    private void createHeadFile() throws IOException {
        Path path = Paths.get(mRepositoryParentFolderLocation, mRepositoryName, ".magit", "Branches", "HEAD");
        Files.createFile(path);
        BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(path.toString()));
        writer.write("master");
        writer.close();
    }

    public HashMap<String, Branch> getmBranches() {
        return mBranches;
    }

    public String getRepositoryName() {
        return mRepositoryName;
    }

    String getmRepositoryParentFolderLocation() {
        return mRepositoryParentFolderLocation;
    }

    void changeBranchPointer(String branchName, Sha1 newCommit) throws IOException {
        FileHandler.writeNewFile(Paths.get(mRepositoryParentFolderLocation, mRepositoryName,".magit","branches", branchName).toString(), newCommit.toString());
    }

    public boolean areThereChanges(Map<FileStatus, SortedSet<Delta.DeltaFileItem>> changes) throws ParseException, IOException {
        final int changesWereMade = 0;

        return changes.get(FileStatus.NEW).size() != changesWereMade ||
                changes.get(FileStatus.EDITED).size() != changesWereMade ||
                changes.get(FileStatus.REMOVED).size() != changesWereMade;
    }

}
