package com.magit.logic.system.managers;

import com.magit.logic.exceptions.CloneException;
import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.system.objects.ClonedRepository;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.utils.file.FileHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public boolean isValid(String repositoryLocation) throws IOException {
        final Path pathToMagit = Paths.get(repositoryLocation,".magit"), pathToHead = Paths.get(repositoryLocation,".magit","branches","HEAD"), repositoryPath = Paths.get(repositoryLocation);
        final String BRANCHES = "BRANCHES";
        return Files.exists(Paths.get(repositoryLocation)) &&
                Files.exists(Paths.get(repositoryLocation)) && Files.exists(pathToMagit) && Files.exists(pathToHead) &&
                !FileHandler.readFile(pathToHead.toString()).isEmpty()
                && Files.exists(Paths.get(pathToMagit.toString(), BRANCHES, FileHandler.readFile(pathToHead.toString())));
    }
}
