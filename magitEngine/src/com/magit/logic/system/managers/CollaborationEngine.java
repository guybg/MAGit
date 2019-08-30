package com.magit.logic.system.managers;

import com.magit.logic.exceptions.*;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.ClonedRepository;
import com.magit.logic.system.objects.RemoteReference;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import com.sun.xml.internal.ws.api.pipe.Engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

public class CollaborationEngine {

    public void cloneRepository(String pathToMagitRepository, String destinationPath, BranchManager branchManager) throws IOException, IllegalPathException, CloneException {
        if(!isValid(pathToMagitRepository)){
            throw new CloneException("Source repository is invalid");
        }
        if(isValid(destinationPath))
            throw new CloneException("There is already a repository at destination location");
        if(pathToMagitRepository.toLowerCase().equals(destinationPath.toLowerCase()))
            throw new CloneException("Destination location is the same as the source repository location, please choose another destination path");
        Repository repository = RepositoryManager.loadRepository(Paths.get(pathToMagitRepository), branchManager);

        ClonedRepository clonedRepository = ClonedRepository.getClone(repository,repository.getRepositoryName(),destinationPath);

        clonedRepository.create();


    }

    public void fetch(Repository repository) throws RemoteReferenceException, IOException, ParseException, PreviousCommitsLimitExceededException, CommitNotFoundException, IllegalPathException {
        if(repository.getRemoteReference() == null)
            throw new RemoteReferenceException("Repository does not have remote reference");
        Repository remoteRepository = RepositoryManager.loadRepository(Paths.get(repository.getRemoteReference().getLocation()), new BranchManager());
        for(Branch branch : remoteRepository.getBranches().values()){
            String remoteBranchName = String.join("/",repository.getRemoteReference().getRepositoryName(),branch.getBranchName());
            if(!repository.getBranches().containsKey(remoteBranchName)){
                Branch remoteBranch = new Branch(
                        remoteBranchName,branch.getPointedCommitSha1().toString(),null, true,false);
                repository.getBranches().put(remoteBranchName,remoteBranch);
                BranchManager.writeBranch(repository,remoteBranchName,remoteBranch.getPointedCommitSha1().toString(),true,false,null);
            }else{
                repository.changeBranchPointer(repository.getBranches().get(remoteBranchName),branch.getPointedCommitSha1());
                BranchManager.writeBranch(repository,remoteBranchName,branch.getPointedCommitSha1().toString(),true,false,null);
            }
        }
        WorkingCopyUtils.updateNewObjects(remoteRepository,repository);
    }

    public boolean isValid(String repositoryLocation) throws IOException {
        final Path pathToMagit = Paths.get(repositoryLocation,".magit"), pathToHead = Paths.get(repositoryLocation,".magit","branches","HEAD"), repositoryPath = Paths.get(repositoryLocation);
        final String BRANCHES = "BRANCHES";
        return Files.exists(Paths.get(repositoryLocation)) &&
                Files.exists(Paths.get(repositoryLocation)) && Files.exists(pathToMagit) && Files.exists(pathToHead) &&
                !FileHandler.readFile(pathToHead.toString()).isEmpty()
                && Files.exists(Paths.get(pathToMagit.toString(), BRANCHES, FileHandler.readFile(pathToHead.toString())));
    }


}
