package com.magit.logic.system;

import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.IllegalPathException;
import com.magit.logic.exceptions.RepositoryNotFoundException;
import com.magit.logic.exceptions.WorkingCopyIsEmptyException;
import com.magit.logic.system.objects.*;
import com.magit.logic.utils.file.FileReader;
import com.magit.logic.utils.file.FileWriter;
import com.magit.logic.utils.file.WorkingCopyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MagitEngine {

    public MagitEngine() {
    }
    private Repository mActiveRepository;
    private Branch mActiveBranch;
    private String mUserName = "Administrator";

    public void updateUserName(String userNameToSet) {
        //1
        mUserName = userNameToSet;
    }

    public void switchRepository(String pathOfRepository) throws RepositoryNotFoundException, IOException, ParseException {
        //3
        Path repositoryPath = Paths.get(pathOfRepository);
        if (Files.exists(repositoryPath, LinkOption.NOFOLLOW_LINKS)) {
            Path pathToMaster = Paths.get(repositoryPath.toString(), ".magit", "branches", "master");
            if (!Files.exists(Paths.get(repositoryPath.toString(), ".magit")) ||
                    !Files.exists(pathToMaster)) {
                throw new RepositoryNotFoundException(repositoryPath.getFileName().toString());
            }
            loadRepository(repositoryPath);
            mActiveBranch = new Branch("master", FileReader.readFile(pathToMaster.toString()));
            Commit commitOfRepository = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
            WorkingCopyUtils workingCopyHandler = new WorkingCopyUtils(mActiveRepository.getRepositoryPath().toString(),
                            mUserName, commitOfRepository.getCreationDate());
            workingCopyHandler.clearWorkingCopyFiles(mActiveRepository.getRepositoryPath());
            workingCopyHandler.unzipWorkingCopy(commitOfRepository, mActiveRepository.getRepositoryPath().toString());
        }
    }

    private void loadRepository(Path repositoryPath) throws IOException {
        mActiveRepository = new Repository(repositoryPath.getFileName().toString()
                , repositoryPath.getParent().toString());
        List<File> branchesFiles = (List<File>) FileUtils.listFiles(
                new File(Paths.get(repositoryPath.toString(), ".magit", "branches").toString()),
                TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        for (File branchFile : branchesFiles) {
            if (!branchFile.getName().equals("HEAD"))
                mActiveRepository.add(branchFile.getName()
                        , new Branch(branchFile.getName(), FileReader.readFile(branchFile.getPath())));
        }
    }

    public String presentCurrentCommitAndHistory() throws RepositoryNotFoundException, IOException, ParseException {
        if (!mActiveRepository.isValid())
            throw new RepositoryNotFoundException(mActiveRepository.getRepositoryName());

        final String seperator = "===============================================================";
        StringBuilder commitContent = new StringBuilder();
        String pathToCommit = mActiveRepository.getCommitPath().toString();
        Commit commitToPresent = Commit.createCommitInstanceByPath(Paths.get(pathToCommit));
        commitContent.append(commitToPresent.toPrintFormat());

        Tree commitTree = WorkingCopyUtils.getWorkingCopyTreeFromCommit(commitToPresent, mActiveRepository.getRepositoryPath().toString());

        LinkedList<FileItem> fileItemQueue = new LinkedList<FileItem>();



        return commitContent.toString();
    }

    public String getBrnchesInfo() throws IOException {
        final String seperator = "============================================";
        StringBuilder branchesContent = new StringBuilder();

        branchesContent.append(String.format("Head Branch : %s%s%s%s",
                FileReader.readFile(mActiveRepository.getHeadPath().toString()),System.lineSeparator(),
                seperator, System.lineSeparator()));
        File branchesDirectory = new File(mActiveRepository.getBranchDirectoryPath().toString());
        File[] files = branchesDirectory.listFiles();
        if (files == null)
            return null;

        for (File branchFile: files) {
            if (!branchFile.getName().equals("HEAD"))
                branchesContent.append(String.format("%s, sha1: %s%s",
                        branchFile.getName(), FileReader.readFile(branchFile.getPath()),System.lineSeparator()));
        }

        return branchesContent.toString();
    }

    public boolean createNewBranch(String branchName)throws IOException {
        if (Files.exists(Paths.get(mActiveRepository.getBranchDirectoryPath().toString(), branchName)))
            return false;

        FileWriter.writeNewFile(
                Paths.get(mActiveRepository.getBranchDirectoryPath().toString(), branchName).toString(), "");
        return true;
    }

    public void createNewRepository(String repositoryName, String fullPath) throws IllegalPathException, IOException {
        Repository repository = new Repository(repositoryName, fullPath);
        repository.create();
        mActiveRepository = repository;
        mActiveBranch = repository.getmBranches().get("master");
    }

    public void commit() throws IOException, WorkingCopyIsEmptyException, ParseException {
        Commit commit = new Commit("test", "Guy", FileType.COMMIT,new Date());
        commit.generate(mActiveRepository, mActiveBranch);
        //testing
       /* WorkingCopyUtils wcw1 = new WorkingCopyUtils(Paths.get(mActiveRepository.getmRepositoryParentFolderLocation(),mActiveRepository.getRepositoryName()).toString(),"guy", commit.getLastModified());
        try {
            Commit commit1 = new Commit("test2", "Shlomo", FileType.COMMIT, new Date());
            commit1.newCommit(mActiveRepository,mActiveBranch);
            Tree t1 = wcw1.getWorkingCopyTreeFromCommit(commit);
            Tree t2 = wcw1.getWorkingCopyTreeFromCommit(commit1);
            Tree t = WorkingCopyUtils.getWcWithOnlyNewchanges(t2,t1);
            t1.getmFiles().removeAll(t2.getmFiles());
            //Objects.deepEquals(t1.getmFiles(),t2.getmFiles());
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
    }
}


