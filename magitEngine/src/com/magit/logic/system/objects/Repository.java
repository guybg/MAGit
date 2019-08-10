package com.magit.logic.system.objects;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.RepositoryAlreadyExistsException;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

public class Repository {

    private final String BRANCHES = "branches";
    private String mRepositoryName;
    private String mRepositoryLocation;
    private HashMap<String, Branch> mBranches;
    private Path pathToRepository;
    private Path pathToMagit;
    private Path pathToHead;

    public Repository(String mRepositoryLocation, String mRepositoryName) {
        this.mRepositoryLocation = mRepositoryLocation;
        this.mBranches = new HashMap<>();
        this.pathToRepository = Paths.get(mRepositoryLocation);
        this.pathToMagit = Paths.get(pathToRepository.toString(), ".magit");
        this.pathToHead = Paths.get(pathToMagit.toString(), BRANCHES, "HEAD");
        this.mRepositoryName = mRepositoryName;
    }

    public void addBranch(String key, Branch value) {
        this.mBranches.put(key, value);
    }

    private Path getBranchPath(String branchName) {
        return Paths.get(pathToMagit.toString(), BRANCHES, branchName);
    }

    public Path getRepositoryPath() {
        return Paths.get(mRepositoryLocation);
    }

    public Path getMagitFolderPath() {
        return pathToMagit;
    }

    public Path getHeadPath() {
        return pathToHead;
    }

    public Path getBranchDirectoryPath() {
        return Paths.get(pathToMagit.toString(), BRANCHES);
    }

    public String[] getAllCommitsOfRepository() throws IOException {
        Path pathToCommitsFile = Paths.get(pathToMagit.toString(), "COMMITS");
        if (Files.notExists(pathToCommitsFile))
            return null;

        return FileHandler.readFile(pathToCommitsFile.toString()).split(System.lineSeparator());
    }

    public boolean isValid() throws IOException {
        return Files.exists(Paths.get(mRepositoryLocation)) &&
                Files.exists(pathToRepository) && Files.exists(pathToMagit) && Files.exists(pathToHead) &&
                !FileHandler.readFile(pathToHead.toString()).isEmpty()
                && Files.exists(Paths.get(pathToMagit.toString(), BRANCHES, mBranches.get("HEAD").getBranchName()));
    }

    public Path getCommitPath() throws IOException {

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

    public Path getObjectsFolderPath() {
        return Paths.get(pathToMagit.toString(), "objects");
    }

    public void create() throws IllegalPathException, IOException {
        final String REPOSITORY_NAME = "REPOSITORY_NAME";
        boolean validPath;
        String headBranch = "master";
        File repository;
        try {
            Path filePath = Paths.get(mRepositoryLocation, ".magit", BRANCHES);
            repository = new File(filePath.toString());
            validPath = repository.mkdirs();
            Path repositoryNamePath = Paths.get(mRepositoryLocation, ".magit", REPOSITORY_NAME);
            FileHandler.writeNewFile(repositoryNamePath.toString(), mRepositoryName);
            if (!mBranches.isEmpty()) {
                for (Map.Entry<String, Branch> branchEntry : mBranches.entrySet()) {
                    if (!branchEntry.getKey().equals("HEAD")) {
                        branchEntry.getValue().create(getRepositoryPath().toString());
                    } else {
                        headBranch = branchEntry.getValue().getBranchName();
                    }
                }
            }
        } catch (InvalidPathException e) {
            throw new IllegalPathException(mRepositoryLocation + " is not a valid path.");

        }

        if (!validPath) {
            if (repository.exists())
                throw new RepositoryAlreadyExistsException(mRepositoryLocation);
            else
                throw new IllegalPathException(mRepositoryLocation + " is not a valid path.");
        }
        if (mBranches.isEmpty()) {
            Branch branch = new Branch("master");
            branch.create(Paths.get(mRepositoryLocation).toString());
            mBranches.put("master", branch);
        }
        createHeadFile(headBranch);
    }

    private void createHeadFile(String branchName) throws IOException {
        Path path = Paths.get(mRepositoryLocation, ".magit", "Branches", "HEAD");
        File branchesPath = new File(path.getParent().toString());
        branchesPath.mkdirs();
        Files.createFile(path);
        try (FileWriter fileWriter = new java.io.FileWriter(path.toString());
             BufferedWriter writer = new BufferedWriter(fileWriter)) {
            writer.write(branchName);
        }
    }

    public HashMap<String, Branch> getmBranches() {
        return mBranches;
    }

    public String getRepositoryName() {
        return mRepositoryName;
    }

    String getmRepositoryLocation() {
        return mRepositoryLocation;
    }

    public void changeBranchPointer(Branch branch, Sha1 newCommit) throws IOException {
        FileHandler.writeNewFile(Paths.get(mRepositoryLocation, ".magit", "branches", branch.getBranchName()).toString(), newCommit.toString());
        branch.setPointedCommitSha1(newCommit);
    }

    public boolean areThereChanges(Map<FileStatus, SortedSet<Delta.DeltaFileItem>> changes) {
        final int changesWereMade = 0;

        return changes.get(FileStatus.NEW).size() != changesWereMade ||
                changes.get(FileStatus.EDITED).size() != changesWereMade ||
                changes.get(FileStatus.REMOVED).size() != changesWereMade;
    }
}
