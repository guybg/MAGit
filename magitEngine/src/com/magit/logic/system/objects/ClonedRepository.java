package com.magit.logic.system.objects;

import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.PreviousCommitsLimitExceededException;
import com.magit.logic.system.MagitEngine;
import com.magit.logic.system.managers.RepositoryManager;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;

public class ClonedRepository extends Repository {
    private Repository repository;
    private ClonedRepository(String mRepositoryLocation, String mRepositoryName) {
        super(mRepositoryLocation, mRepositoryName);
    }



    @Override
    protected void setRepositoryName(String name) {
        super.setRepositoryName(name);
    }

    public static ClonedRepository getClone(Repository toClone, String clonedRepositoryName, String clonedRepositoryLocation){
        Repository repository = toClone.clone();
        ClonedRepository clonedRepository = new ClonedRepository(clonedRepositoryLocation,clonedRepositoryName);
        clonedRepository.repository = repository;
        clonedRepository.setRepositoryName(clonedRepositoryName);
        clonedRepository.setBranches(repository.getBranches());
        clonedRepository.setRemoteReference(new RemoteReference(repository.getRepositoryName(),repository.getmRepositoryLocation()));
        clonedRepository.createRemoteTrackingBranchForHead();
        return clonedRepository;
    }

    public void create() throws IOException, IllegalPathException {
        createInitialMagitFolder();
        createObjectsFiles();
        createCommitsFile();
        try {
            RepositoryManager.unzipHeadBranchCommitWorkingCopy(this);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (PreviousCommitsLimitExceededException e) {
            e.printStackTrace();
        }
    }

    private void createObjectsFiles() throws IOException {
        this.getObjectsFolderPath().toFile().mkdirs();
        FileUtils.copyDirectory(repository.getObjectsFolderPath().toFile(),this.getObjectsFolderPath().toFile());
    }

    private void createInitialMagitFolder() throws IOException, IllegalPathException {
        super.create();
    }

    private void createCommitsFile() throws IOException {
        Path pathToOriginalCommitsFile = Paths.get(repository.getMagitFolderPath().toString(), "COMMITS");
        Path pathToClonedCommitsFile = Paths.get(this.getMagitFolderPath().toString(), "COMMITS");
        if(pathToOriginalCommitsFile.toFile().exists())
            FileUtils.copyFile(pathToOriginalCommitsFile.toFile(), new File(pathToClonedCommitsFile.toString()));
    }

    private void createRemoteTrackingBranchForHead(){
        Branch branch = repository.getBranches().get("HEAD");
        getBranches().put(branch.getBranchName(),branch);
    }
}
