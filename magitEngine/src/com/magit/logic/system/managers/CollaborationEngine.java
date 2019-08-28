package com.magit.logic.system.managers;

import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.system.objects.ClonedRepository;
import com.magit.logic.system.objects.Repository;

import java.io.IOException;
import java.nio.file.Paths;

public class CollaborationEngine {

    public void cloneRepository(String pathToMagitRepository, String destinationPath, BranchManager branchManager, String clonedRepositoryName) throws IOException, IllegalPathException {
        Repository repository = RepositoryManager.loadRepository(Paths.get(pathToMagitRepository), branchManager);

        ClonedRepository clonedRepository = ClonedRepository.getClone(repository,clonedRepositoryName,destinationPath);

        clonedRepository.create();


    }
}
