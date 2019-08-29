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
import java.util.stream.Collectors;

public class Repository implements Cloneable{

    private final String BRANCHES = "branches";
    private String mRepositoryName;
    private String mRepositoryLocation;
    private HashMap<String, Branch> mBranches;
    private Path pathToRepository;
    private Path pathToMagit;
    private Path pathToHead;
    private RemoteReference remoteReference;


    public Repository(String mRepositoryLocation, String mRepositoryName) {
        this.mRepositoryLocation = mRepositoryLocation;
        this.mBranches = new HashMap<>();
        this.pathToRepository = Paths.get(mRepositoryLocation);
        this.pathToMagit = Paths.get(pathToRepository.toString(), ".magit");
        this.pathToHead = Paths.get(pathToMagit.toString(), BRANCHES, "HEAD");
        this.mRepositoryName = mRepositoryName;
    }

    public void setRemoteReference(RemoteReference remoteReference) {
        this.remoteReference = remoteReference;
    }

    @Override
    public Repository clone() {
        try {
            Repository clonedRepository = (Repository)super.clone();
            clonedRepository.mBranches = new HashMap<>();
            for (Map.Entry<String, Branch> keyValue : mBranches.entrySet()) {
                if(keyValue.getKey().equals("HEAD")) {
                    clonedRepository.mBranches.put(keyValue.getKey(),keyValue.getValue());
                    continue;
                }
                String remoteBranchName = String.format("%s/%s", getRepositoryName(),keyValue.getKey());
                Branch branch = new Branch(remoteBranchName);
                branch.setPointedCommitSha1(keyValue.getValue().getPointedCommitSha1());
                branch.setIsRemote(true);
                clonedRepository.mBranches.put(remoteBranchName,branch);
            }
            remoteReference = new RemoteReference(mRepositoryName,mRepositoryLocation);
            return clonedRepository;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }


        return null;
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
        String sha1OfCommit = readBranchContent(pathToBranchFile.toFile()).get("sha1");
        return Paths.get(pathToMagit.toString(), "objects", sha1OfCommit);
    }

    public static HashMap<String, String> readBranchContent(File branchFile) throws IOException {
        final String sha1 = "sha1", isRemote = "isRemote", isTracking = "isTracking", trackingAfter = "trackingAfter";
        String content = FileHandler.readFile(branchFile.getPath());
        String[] branchContentArray = content.split(System.lineSeparator());
        HashMap<String,String> branchContent = new HashMap<>();
        branchContent.put(sha1,branchContentArray[0]);
        branchContent.put(isRemote, branchContentArray[1]);
        branchContent.put(isTracking, branchContentArray[2]);
        branchContent.put(trackingAfter, branchContentArray[3]);
        return branchContent;
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
            throw new IllegalPathException(mRepositoryLocation + " is not a valid path.");
        }
        if (mBranches.isEmpty()) {
            Branch branch = new Branch("master");
            branch.create(Paths.get(mRepositoryLocation).toString());
            mBranches.put("master", branch);
            mBranches.put("HEAD", branch);
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

    public HashMap<String, Branch> getBranches() {
        return mBranches;
    }

    public String getRepositoryName() {
        return mRepositoryName;
    }

    String getmRepositoryLocation() {
        return mRepositoryLocation;
    }

    public void changeBranchPointer(Branch branch, Sha1 newCommit) throws IOException {
        HashMap<String,String> branchContent = Repository.readBranchContent(Paths.get(mRepositoryLocation, ".magit", "branches", branch.getBranchName()).toFile());
        branchContent.replace("sha1", newCommit.toString());
        String newBranchContent = branchContent.values().stream().collect(Collectors.joining(System.lineSeparator()));
        FileHandler.writeNewFile(Paths.get(mRepositoryLocation, ".magit", "branches", branch.getBranchName()).toString(), newBranchContent);
        branch.setPointedCommitSha1(newCommit);
    }

    public boolean areThereChanges(Map<FileStatus, SortedSet<Delta.DeltaFileItem>> changes) {
        final int changesWereMade = 0;

        return changes.get(FileStatus.NEW).size() != changesWereMade ||
                changes.get(FileStatus.EDITED).size() != changesWereMade ||
                changes.get(FileStatus.REMOVED).size() != changesWereMade;
    }

    public boolean headBranchHasUnhandledMerge(){
        return Files.exists(Paths.get(getMagitFolderPath().toString(),".merge",getBranches().get("HEAD").getBranchName()));
    }

    protected void setRepositoryName(String name){
        mRepositoryName = name;
    }

    protected void setBranches(HashMap<String,Branch> branches){
        mBranches = branches;
    }
}
