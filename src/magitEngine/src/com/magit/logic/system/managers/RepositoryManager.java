package com.magit.logic.system.managers;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.logic.exceptions.WorkingCopyIsEmptyException;
import com.magit.logic.exceptions.WorkingCopyStatusNotChangedComparedToLastCommitException;
import com.magit.logic.system.objects.Branch;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.Repository;
import com.magit.logic.system.objects.Tree;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class RepositoryManager {
    private Repository mActiveRepository;

    public void setUserName(String userName) {
        mActiveRepository.setUpdaterName(userName);
    }

    public Repository getRepository() {
        return mActiveRepository;
    }

    public void switchRepository(String pathOfRepository, BranchManager branchManager) throws RepositoryNotFoundException, IOException, ParseException {
        if (!isValidRepository(pathOfRepository))
            throw new RepositoryNotFoundException("repository Not Found");

        Path repositoryPath = Paths.get(pathOfRepository);
        loadRepository(repositoryPath, branchManager);
        if (mActiveRepository.getCommitPath() != null) {

            Commit commitOfRepository = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
            if (commitOfRepository == null)
                return;

            WorkingCopyUtils workingCopyHandler = new WorkingCopyUtils(mActiveRepository.getRepositoryPath().toString(),
                    mActiveRepository.getUpdaterName(), commitOfRepository.getCreationDate());
            workingCopyHandler.clearWorkingCopyFiles(mActiveRepository.getRepositoryPath());
            workingCopyHandler.unzipWorkingCopyFromCommit(commitOfRepository, mActiveRepository.getRepositoryPath().toString());
        }
    }

    private boolean isValidRepository(String repositoryPath) {
        final String magit = ".magit";

        return Files.exists(Paths.get(repositoryPath)) &&
                Files.exists(Paths.get(repositoryPath, magit)) &&
                Files.exists(Paths.get(repositoryPath, magit, "branches", "HEAD"));
    }

    private void loadRepository(Path repositoryPath, BranchManager branchManager) throws IOException {
        mActiveRepository = new Repository(repositoryPath.getFileName().toString()
                , repositoryPath.getParent().toString());
        List<File> branchesFiles = (List<File>) FileUtils.listFiles(
                new File(Paths.get(repositoryPath.toString(), ".magit", "branches").toString()),
                TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

        for (File branchFile : branchesFiles) {
            if (!branchFile.getName().equals("HEAD"))
                mActiveRepository.add(branchFile.getName()
                        , new Branch(branchFile.getName(), FileHandler.readFile(branchFile.getPath())));
            else {
                branchManager.loadBranch(branchFile);
                mActiveRepository.add(branchFile.getName(), branchManager.getActiveBranch());
            }
        }
    }


    public String presentCurrentCommitAndHistory() throws RepositoryNotFoundException, IOException, ParseException {
        if (!mActiveRepository.isValid())
            throw new RepositoryNotFoundException(mActiveRepository.getRepositoryName());

        Commit commit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
        if (commit == null)
            return null;

        WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils
                (mActiveRepository.getRepositoryPath().toString(), mActiveRepository.getUpdaterName(), commit.getCreationDate());
        return workingCopyUtils.getWorkingCopyContent(WorkingCopyUtils.getWorkingCopyTreeFromCommit(commit, mActiveRepository.getRepositoryPath().toString()));
    }

    public void createNewRepository(String repositoryName, String fullPath, Branch activeBranch) throws IllegalPathException, IOException {
        Repository repository = new Repository(repositoryName, fullPath);
        repository.create();
        mActiveRepository = repository;
        activeBranch = repository.getmBranches().get("master");
    }

    public void commit(String commitMessage, String creator, Branch mActiveBranch) throws IOException, WorkingCopyIsEmptyException, ParseException, WorkingCopyStatusNotChangedComparedToLastCommitException {
        Commit commit = new Commit(commitMessage, creator, FileType.COMMIT, new Date());
        commit.generate(mActiveRepository, mActiveBranch);
    }

    public MultiValuedMap<FileStatus, String> checkDifferenceBetweenCurrentWCandLastCommit() throws IOException, ParseException {
        WorkingCopyUtils wcw = new WorkingCopyUtils(mActiveRepository.getRepositoryPath().toString(),
                mActiveRepository.getUpdaterName(), new Date());

        Tree curWc = wcw.getWc();
        Commit lastCommit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());

        Tree wcOfLastCommitToCompare = WorkingCopyUtils.getWorkingCopyTreeFromCommit(lastCommit, mActiveRepository.getRepositoryPath().toString());
        return WorkingCopyUtils.getWorkingCopyStatus(curWc, wcOfLastCommitToCompare, mActiveRepository.getRepositoryPath().toString());
    }

    public String getBranchesInfo() throws IOException {
        final String seperator = "============================================";
        StringBuilder branchesContent = new StringBuilder();

        branchesContent.append(String.format("Head Branch : %s%s%s%s",
                FileHandler.readFile(mActiveRepository.getHeadPath().toString()),System.lineSeparator(),
                seperator, System.lineSeparator()));
        File branchesDirectory = new File(mActiveRepository.getBranchDirectoryPath().toString());
        File[] files = branchesDirectory.listFiles();
        if (files == null)
            return null;

        for (File branchFile: files) {
            if (!branchFile.getName().equals("HEAD"))
                branchesContent.append(String.format("%s, sha1: %s%s",
                        branchFile.getName(), FileHandler.readFile(branchFile.getPath()),System.lineSeparator()));
        }

        return branchesContent.toString();
    }
}
