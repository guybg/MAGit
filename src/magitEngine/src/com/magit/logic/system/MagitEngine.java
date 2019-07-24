package com.magit.logic.system;

import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.utils.file.FileReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

public class MagitEngine {

    public MagitEngine() {
    }
    private Repository mActiveRepository;
    private Branch mActiveBranch;
    private String mUserName = "Administrator";


    public void updateUserName(String userNameToSet) {
        mUserName = userNameToSet;
    }

    public void switchRepository(String pathOfRepository) throws RepositoryNotFoundException, IOException {
        Path repositoryPath = Paths.get(pathOfRepository);
        if (Files.exists(repositoryPath, LinkOption.NOFOLLOW_LINKS)) {
            Path pathToMaster = Paths.get(repositoryPath.toString(),".magit", "branches", "master");
            if (!Files.exists(Paths.get(repositoryPath.toString(), ".magit")) ||
                    !Files.exists(pathToMaster)) {
                throw new RepositoryNotFoundException(repositoryPath.getFileName().toString());
            }
            mActiveRepository = new Repository(repositoryPath.getFileName().toString()
                    , repositoryPath.getParent().toString());

            List<File> branchesFiles = (List<File>) FileUtils.listFiles(
                    new File(Paths.get(repositoryPath.toString(),".magit", "branches").toString()),
                    TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

            for (File branchFile : branchesFiles) {
                if (!branchFile.getName().equals("HEAD"))
                    mActiveRepository.add(branchFile.getName()
                    ,new Branch(branchFile.getName(), FileReader.readFile(branchFile.getPath())));
            }
            mActiveBranch = new Branch("master", FileReader.readFile(pathToMaster.toString()));
        } else {
            throw new RepositoryNotFoundException(repositoryPath.getFileName().toString());
        }
    }

    public void createNewRepository(String repositoryName, String fullPath) throws IllegalPathException, IOException {
        Repository repository = new Repository(repositoryName, fullPath);
        repository.create();
        mActiveRepository = repository;
        mActiveBranch = repository.getmBranches().get("master");
    }

    public void commit() throws IOException {
        Commit commit = new Commit("test", "Guy", FileType.COMMIT,new Date());
        commit.newCommit(mActiveRepository, mActiveBranch);
    }
}


