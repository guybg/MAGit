package com.magit.logic.system.managers;

import com.magit.logic.system.objects.Repository;

import java.io.IOException;
import java.nio.file.Paths;

public class CollaborationEngine {

    public void cloneRepository(String pathToMagitRepository, String destinationPath, BranchManager branchManager) throws IOException {
        Repository repository = RepositoryManager.loadRepository(Paths.get(pathToMagitRepository), branchManager);

        Repository cloned = repository.clone();



    }
}
