package com.magit.logic.system;

import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.utils.file.WorkingCopyWalker;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;

public class MagitEngine {

    public MagitEngine() {
    }
    private Repository mActiveRepository;
    private Branch mActiveBranch;


    public void createNewRepository(String repositoryName, String fullPath) throws IllegalPathException, IOException {
        Repository repository = new Repository(repositoryName, fullPath);
        repository.create();
        mActiveRepository = repository;
        mActiveBranch = repository.getmBranches().get("master");
    }

    public void commit() throws IOException, ParseException {
        Commit commit = new Commit("test", "Guy", FileType.COMMIT,new Date());
        commit.newCommit(mActiveRepository, mActiveBranch);
        WorkingCopyWalker wcw = new WorkingCopyWalker(Paths.get(mActiveRepository.getmRepositoryParentFolderLocation(), mActiveRepository.getRepositoryName()).toString(), "Guy", commit.getmCommitDate());
        wcw.unzipWorkingCopy(commit, Paths.get("C:", "testingRep", "testRep8", ".magit", "tmp").toString());

    }
}


