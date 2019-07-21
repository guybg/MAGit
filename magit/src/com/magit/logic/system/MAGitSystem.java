package com.magit.logic.system;

import com.magit.logic.handle.exceptions.IllegalPathException;
import com.magit.logic.handle.exceptions.RepositoryAlreadyExistsException;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Repository;

import java.io.IOException;

public class MAGitSystem {
    private Repository mActiveRepository;
    private Branch mActiveBranch;

    public MAGitSystem() {
    }

    public void createNewRepository(String repositoryName, String fullPath) throws RepositoryAlreadyExistsException, IllegalPathException , IOException {
        Repository repository = new Repository(repositoryName, fullPath);
        repository.create();

    }
}


