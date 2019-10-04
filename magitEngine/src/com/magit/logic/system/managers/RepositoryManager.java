package com.magit.logic.system.managers;

import com.google.gson.Gson;
import com.magit.logic.enums.FileStatus;
import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.objects.*;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.FileItemHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import com.magit.logic.utils.jstree.JsTreeAttributes;
import com.magit.logic.utils.jstree.JsTreeItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class RepositoryManager {
    private final String EMPTY = "";
    private Repository mActiveRepository;

    public RepositoryManager() {
    }

    public RepositoryManager(Path repositoryPath, BranchManager branchManager) throws IOException {
        initializeActiveRepository(repositoryPath, branchManager);
    }

    public Repository getRepository() {
        return mActiveRepository;
    }

    public String getHeadBranch() {
        return mActiveRepository.getBranches().get("HEAD").getBranchName();
    }

    public void setActiveRepository(Repository mActiveRepository) {
        this.mActiveRepository = mActiveRepository;
    }

    public void switchRepository(String pathOfRepository, BranchManager branchManager, String userName) throws RepositoryNotFoundException, IOException, ParseException {
        if (!isValidRepository(pathOfRepository))
            throw new RepositoryNotFoundException("Repository not found or corrupted.");

        Path repositoryPath = Paths.get(pathOfRepository);
        initializeActiveRepository(repositoryPath, branchManager);
    }

    private boolean isValidRepository(String repositoryPath) throws IOException {
        final String magit = ".magit";

        return Files.exists(Paths.get(repositoryPath)) &&
                Files.exists(Paths.get(repositoryPath, magit)) &&
                Files.exists(Paths.get(repositoryPath, magit, "branches", "HEAD")) &&
                !FileHandler.readFile(Paths.get(repositoryPath, magit, "branches", "HEAD").toString()).isEmpty() &&
                Files.exists((Paths.get(repositoryPath, magit, "REPOSITORY_NAME")));
    }
    // (todo) handle load of remote branches, right now it skips them.
    public static Repository loadRepository(Path repositoryPath, BranchManager branchManager) throws IOException {

        String repositoryName = FileHandler.readFile(Paths.get(repositoryPath.toString(), ".magit", "REPOSITORY_NAME").toString());
        Repository repository = new Repository(repositoryPath.toString(), repositoryName);
        List<File> branchesFiles = (List<File>) FileUtils.listFiles(
                new File(Paths.get(repositoryPath.toString(), ".magit", "branches").toString()),
                TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        if(Files.exists(Paths.get(repositoryPath.toString(), ".magit", "REMOTE_REFERENCE"))){
            String[] remoteReferenceInfo = FileHandler.readFile(Paths.get(repositoryPath.toString(), ".magit", "REMOTE_REFERENCE").toString()).split(System.lineSeparator());
            RemoteReference remoteReference = new RemoteReference(remoteReferenceInfo[0],remoteReferenceInfo[1]);
            repository.setRemoteReference(remoteReference);
        }
        for (File branchFile : branchesFiles) {
            if (!branchFile.getName().equals("HEAD")) {
                if(!branchFile.getParentFile().getName().equals("branches")){
                    for(File branchInDirectory : Objects.requireNonNull(branchFile.getParentFile().listFiles())){
                        loadBranchFromFile(branchInDirectory,repository, true);
                    }
                }else {
                    loadBranchFromFile(branchFile, repository, false);
                }
            }
        }
        for (File branchFile : branchesFiles) {
            if (branchFile.getName().equals("HEAD")){
                branchManager.setActiveBranch(repository.getBranches().get(FileHandler.readFile(branchFile.getPath())));
                repository.addBranch(branchFile.getName(), branchManager.getActiveBranch());
            }
        }
        return repository;
    }

    private static void loadBranchFromFile(File branchFile, Repository repository, Boolean isBranchInDirectory) throws IOException {
        final String sha1 = "sha1", isRemote = "isRemote", isTracking = "isTracking", trackingAfter = "trackingAfter";

        String trackingAfterValue = null;
        HashMap<String,String> branchContent = Repository.readBranchContent(branchFile);
        if(!branchContent.get(trackingAfter).equals("null")){
            trackingAfterValue = branchContent.get(trackingAfter);
        }
        String branchName = branchFile.getName();
        if(isBranchInDirectory){
            branchName = String.join("\\",branchFile.getParentFile().getName(),branchName);
        }
        repository.addBranch(branchName
                , new Branch(branchName, branchContent.get(sha1),trackingAfterValue, Boolean.valueOf(branchContent.get(isRemote)), Boolean.valueOf(branchContent.get(isTracking))));
    }


    private void initializeActiveRepository(Path repositoryPath, BranchManager branchManager) throws IOException {

        mActiveRepository = loadRepository(repositoryPath, branchManager);
    }

    public void unzipHeadBranchCommitWorkingCopy() throws IOException, ParseException, PreviousCommitsLimitExceededException {
        Commit commit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
        if (commit == null) return;
        WorkingCopyUtils.unzipWorkingCopyFromCommit(commit, mActiveRepository.getRepositoryPath().toString(),
                mActiveRepository.getRepositoryPath().toString());
    }

    public static void unzipHeadBranchCommitWorkingCopy(Repository repository) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        Commit commit = Commit.createCommitInstanceByPath(repository.getCommitPath());
        if (commit == null) return;
        WorkingCopyUtils.unzipWorkingCopyFromCommit(commit, repository.getRepositoryPath().toString(),
                repository.getRepositoryPath().toString());
    }

    public String presentCurrentCommitAndHistory()
            throws RepositoryNotFoundException, IOException, ParseException, CommitNotFoundException, PreviousCommitsLimitExceededException {
        if (!mActiveRepository.isValid())
            throw new RepositoryNotFoundException("Repository at location " + mActiveRepository.getRepositoryPath().toString() + " is corrupted.");

        Commit commit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
        if (commit == null)
            throw new CommitNotFoundException("There's no commit history to show, please add some files and commit them");

        return WorkingCopyUtils.getWorkingCopyContent(WorkingCopyUtils.getWorkingCopyTreeFromCommit(commit, mActiveRepository.getRepositoryPath().toString()), mActiveRepository.getRepositoryPath().toString(), commit.getLastUpdater());
    }

    public ObservableList<FileItemInfo> guiPresentCurrentCommitAndHistory()
              throws RepositoryNotFoundException, IOException, ParseException, CommitNotFoundException, PreviousCommitsLimitExceededException {
            if (!mActiveRepository.isValid())
                throw new RepositoryNotFoundException("Repository at location " + mActiveRepository.getRepositoryPath().toString() + " is corrupted.");

            Commit commit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
            if (commit == null)
                throw new CommitNotFoundException("There's no commit history to show, please add some files and commit them");

            return WorkingCopyUtils.guiGetWorkingCopyContent(WorkingCopyUtils.getWorkingCopyTreeFromCommit(commit, mActiveRepository.getRepositoryPath().toString()), mActiveRepository.getRepositoryPath().toString(), commit.getLastUpdater());
    }

    public void createNewRepository(String fullPath, BranchManager branchManager, String repositoryName) throws IllegalPathException, IOException, RepositoryAlreadyExistsException {
        if(isValidRepository(fullPath))
            throw new RepositoryAlreadyExistsException(fullPath);
        Repository repository = new Repository(fullPath, repositoryName);
        repository.create();
        mActiveRepository = repository;
        branchManager.setActiveBranch(repository.getBranches().get("master"));
    }

    public void commit(String commitMessage, String creator, Branch mActiveBranch) throws IOException, WorkingCopyIsEmptyException, ParseException, WorkingCopyStatusNotChangedComparedToLastCommitException, PreviousCommitsLimitExceededException, FastForwardException {
        Commit commit = new Commit(commitMessage, creator, FileType.COMMIT, new Date());
        commit.generate(mActiveRepository, mActiveBranch);
    }

    public Map<FileStatus, SortedSet<Delta.DeltaFileItem>> checkDifferenceBetweenCurrentWCAndLastCommit() throws IOException, ParseException, PreviousCommitsLimitExceededException {
        WorkingCopyUtils workingCopyUtils = new WorkingCopyUtils(mActiveRepository.getRepositoryPath().toString(),
                EMPTY, new Date());
        SortedSet<Delta.DeltaFileItem> curWcDeltaFiles;
        SortedSet<Delta.DeltaFileItem> commitDeltaFiles;
        curWcDeltaFiles = workingCopyUtils.getAllDeltaFilesFromCurrentWc();
        Commit lastCommit = Commit.createCommitInstanceByPath(mActiveRepository.getCommitPath());
        commitDeltaFiles = WorkingCopyUtils.getDeltaFileItemSetFromCommit(lastCommit, mActiveRepository.getRepositoryPath().toString());
        if (commitDeltaFiles == null) commitDeltaFiles = new TreeSet<>();
        return WorkingCopyUtils.getDifferencesBetweenCurrentWcAndLastCommit(curWcDeltaFiles, commitDeltaFiles);
    }

    public Map<FileStatus, SortedSet<Delta.DeltaFileItem>> checkDifferencesBetweenTwoCommits(String sha1OfFirstCommit, String sha1OfSecondCommit) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        SortedSet<Delta.DeltaFileItem> firstCommitDeltaFiles;
        SortedSet<Delta.DeltaFileItem> secondCommitDeltaFiles;
        Commit firstCommit = Commit.createCommitInstanceByPath(Paths.get(mActiveRepository.getObjectsFolderPath().toString(), sha1OfFirstCommit));
        Commit secondCommit = Commit.createCommitInstanceByPath(Paths.get(mActiveRepository.getObjectsFolderPath().toString(), sha1OfSecondCommit));
        firstCommitDeltaFiles = WorkingCopyUtils.getDeltaFileItemSetFromCommit(firstCommit, mActiveRepository.getRepositoryPath().toString());
        secondCommitDeltaFiles = WorkingCopyUtils.getDeltaFileItemSetFromCommit(secondCommit, mActiveRepository.getRepositoryPath().toString());
        if (firstCommitDeltaFiles == null) firstCommitDeltaFiles = new TreeSet<>();
        if (secondCommitDeltaFiles == null) secondCommitDeltaFiles = new TreeSet<>();
        return WorkingCopyUtils.getDifferencesBetweenCurrentWcAndLastCommit(firstCommitDeltaFiles, secondCommitDeltaFiles);
    }

    public String getBranchesInfo() throws IOException, ParseException, PreviousCommitsLimitExceededException {
        final String separator = "============================================";
        StringBuilder branchesContent = new StringBuilder();
        String headBranch = FileHandler.readFile(mActiveRepository.getHeadPath().toString());
        File branchesDirectory = new File(mActiveRepository.getBranchDirectoryPath().toString());
        File[] files = branchesDirectory.listFiles();
        if (files == null)
            return null;


        for (File branchFile : files) {
            String commitSha1 = Repository.readBranchContent(branchFile).get("sha1");
            String commitMessage = "none";
            if (commitSha1.isEmpty()) commitSha1 = "none";
            if (!branchFile.getName().equals("HEAD")) {
                Commit commit = Commit.createCommitInstanceByPath(Paths.get(mActiveRepository.getObjectsFolderPath().toString(), commitSha1));
                if (commit != null) {
                    commitMessage = commit.getCommitMessage();
                }
                branchesContent.append(String.format("Branch name: %s%s%s", branchFile.getName().equals(headBranch) ? "[HEAD] " : EMPTY, branchFile.getName(), System.lineSeparator()));
                branchesContent.append(String.format("Commit Sha1: %s%s", commitSha1, System.lineSeparator()));
                branchesContent.append(String.format("Commit Message: %s%s", commitMessage, System.lineSeparator()));
                branchesContent.append(String.format("%s%s", separator, System.lineSeparator()));
            }
        }
        return branchesContent.toString();
    }


    public String getWorkingCopyStatus(String userName) throws IOException, ParseException, PreviousCommitsLimitExceededException {
        StringBuilder workingCopyStatusContent = new StringBuilder();
        workingCopyStatusContent.append(String.format("Repository name: %s%s", mActiveRepository.getRepositoryName(), System.lineSeparator()));
        workingCopyStatusContent.append(String.format("Repository location: %s%s", mActiveRepository.getRepositoryPath(), System.lineSeparator()));
        workingCopyStatusContent.append(String.format("Active user: %s%s", userName, System.lineSeparator()));
        workingCopyStatusContent.append(String.format("%s", System.lineSeparator()));

        Map<FileStatus, SortedSet<Delta.DeltaFileItem>> differences = checkDifferenceBetweenCurrentWCAndLastCommit();
        if (differences.values().stream().allMatch(Set::isEmpty))
            return workingCopyStatusContent.append(String.format("%s%s", "There are no open changes.", System.lineSeparator())).toString();
        workingCopyStatusContent.append(String.format("New Files: %s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("==========%s", System.lineSeparator()));
        if (differences.get(FileStatus.NEW).isEmpty())
            workingCopyStatusContent.append("-NONE-" + System.lineSeparator());
        for (Delta.DeltaFileItem item : differences.get(FileStatus.NEW)) {
            workingCopyStatusContent.append(String.format("(+) %s%s", item.getFullPath(), System.lineSeparator()));
        }
        workingCopyStatusContent.append(String.format("%s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("Edited Files: %s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("==========%s", System.lineSeparator()));
        if (differences.get(FileStatus.EDITED).isEmpty())
            workingCopyStatusContent.append("-NONE-" + System.lineSeparator());
        for (Delta.DeltaFileItem item : differences.get(FileStatus.EDITED)) {
            workingCopyStatusContent.append(String.format("%s%s", item.getFullPath(), System.lineSeparator()));
        }
        workingCopyStatusContent.append(String.format("%s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("Deleted Files: %s", System.lineSeparator()));
        workingCopyStatusContent.append(String.format("==========%s", System.lineSeparator()));
        if (differences.get(FileStatus.REMOVED).isEmpty())
            workingCopyStatusContent.append("-NONE-" + System.lineSeparator());
        for (Delta.DeltaFileItem item : differences.get(FileStatus.REMOVED)) {
            workingCopyStatusContent.append(String.format("(-) %s%s", item.getFullPath(), System.lineSeparator()));
        }
        workingCopyStatusContent.append(String.format("%s", System.lineSeparator()));
        return workingCopyStatusContent.toString();
    }


    public ObservableList<Branch> getBranches() {
        return FXCollections.observableArrayList(mActiveRepository.getBranches()
                .entrySet().stream().filter(e -> !e.getKey()
                        .equals("HEAD")).map(Map.Entry::getValue).collect(Collectors.toList()));
    }

    public ArrayList<String> guiGetRepositoryCommitList() throws IOException {
           return new ArrayList<>(Arrays.asList(mActiveRepository.getAllCommitsOfRepository()));
    }

    public String guiGetBranchInfo(Branch branch) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        String sha1OfCommit = branch.getPointedCommitSha1().toString();
        Path pathToCommit = Paths.get(mActiveRepository.getObjectsFolderPath().toString(), sha1OfCommit);
        Commit commitOfBranch = Commit.createCommitInstanceByPath(pathToCommit);
        String commitMessage = commitOfBranch == null ? "" : commitOfBranch.getCommitMessage();
        String info =  String.format("Branch Name: %s%sSha1: %s%sCommit Message:%s%sRemote: %s",
                branch.getBranchName(), System.lineSeparator(), branch.getPointedCommitSha1().toString(), System.lineSeparator(),
                commitMessage, System.lineSeparator(),branch.getIsRemote());
        if(branch.getIsTracking()){
            info = info.concat(String.format("%sTracking after: %s", System.lineSeparator(),branch.getTrackingAfter()));
        }
        return info;
    }

    public boolean hasRemoteReference(){
        if(mActiveRepository.getRemoteReference() == null){
            return false;
        }
        return true;
    }
    public ArrayList<String> getBranchCommits(String branchName) throws IOException, CommitNotFoundException {
        HashSet<String> sha1sOfCommit = new HashSet<>();
        LinkedList<String> sha1Queue = new LinkedList<>();
        String sha1OfActiveBranch = getRepository().getBranches().get(branchName).getPointedCommitSha1().toString();//mBranchManager.getActiveBranch().getPointedCommitSha1().toString();
        if(sha1OfActiveBranch.isEmpty())
            throw new CommitNotFoundException("Current branch has no commits to show.");
        String pathToObjectsFolder = getRepository().getObjectsFolderPath().toString();
        sha1Queue.add(sha1OfActiveBranch);
        sha1sOfCommit.add(sha1OfActiveBranch);

        while (!sha1Queue.isEmpty()) {
            String currentCommitSha1 = sha1Queue.poll();
            String commitFileContent = FileItemHandler.zipToString(pathToObjectsFolder, currentCommitSha1);
            String previousCommitsHistory = commitFileContent.split(System.lineSeparator())[1];
            String[] separatedCommitsLine = previousCommitsHistory.split(" = ");
            if (separatedCommitsLine.length <= 1)
                continue;
            String sha1sToAdd = separatedCommitsLine[1];
            List<String> sha1List = Arrays.asList(sha1sToAdd.split(";"));
            sha1sOfCommit.addAll(sha1List);
            sha1Queue.addAll(sha1List);
        }

        return new ArrayList<>(sha1sOfCommit);
    }
    public ArrayList<String> getCommitsDeltaBetweenTwoBranches(String baseBranch, String targetBranch) throws PreviousCommitsLimitExceededException, RepositoryNotFoundException, ParseException, IOException, CommitNotFoundException {
        ArrayList<String> targetSha1s = getBranchCommits(targetBranch);
        ArrayList<String> baseSha1s = getBranchCommits(baseBranch);

        return new ArrayList<>(CollectionUtils.subtract(targetSha1s, baseSha1s));
    }
    public Map<FileStatus, SortedSet<Delta.DeltaFileItem>> getOverallCommitsDiff(String baseBranch, String targetBranch) throws PreviousCommitsLimitExceededException, IOException, CommitNotFoundException, ParseException, RepositoryNotFoundException {
        SortedMap<Date, Commit> orderedCommits = new TreeMap<>();
        Map<FileStatus, SortedSet<Delta.DeltaFileItem>> totalDiff = new HashMap<>();
        if(!getRepository().getBranches().get(baseBranch).getPointedCommitSha1().toString().isEmpty()){
            Commit baseCurrentCommit = Commit.createCommitInstanceByPath(Paths.get(getRepository().getObjectsFolderPath().toString(),getRepository().getBranches().get(baseBranch).getPointedCommitSha1().toString()));
            orderedCommits.put(Objects.requireNonNull(baseCurrentCommit).getCreationDate(),baseCurrentCommit);
        }

        ArrayList<String> sha1s = getCommitsDeltaBetweenTwoBranches(baseBranch,targetBranch);
        for (String sha1 : sha1s) {
            Commit currentCommit = Commit.createCommitInstanceByPath(Paths.get(getRepository().getObjectsFolderPath().toString(), sha1));
            orderedCommits.put(Objects.requireNonNull(currentCommit).getCreationDate(),currentCommit);
        }
        Iterator<Map.Entry<Date, Commit>> entries = orderedCommits.entrySet().iterator();
        Map.Entry<Date, Commit> entryFirst = null;
        Map.Entry<Date, Commit> entrySecond = null;
        while (entries.hasNext()) {
            if(entryFirst == null)
                entryFirst = entries.next();
            else{
                entryFirst = entrySecond;
            }
            if(!entries.hasNext()) break;
            entrySecond = entries.next();
            Map<FileStatus, SortedSet<Delta.DeltaFileItem>> diff = checkDifferencesBetweenTwoCommits(entrySecond.getValue().getSha1(),entryFirst.getValue().getSha1());
            if(totalDiff.isEmpty()){
                totalDiff = diff;
            }else{
                for(Map.Entry<FileStatus,SortedSet<Delta.DeltaFileItem>> entry : diff.entrySet()){
                    for(Delta.DeltaFileItem item : entry.getValue()){
                        for(Map.Entry<FileStatus,SortedSet<Delta.DeltaFileItem>> totalEntry : totalDiff.entrySet()){
                            ArrayList<Delta.DeltaFileItem> itemsToRemove = totalEntry.getValue().stream().filter(a->a.getFullPath().toLowerCase().equals(item.getFullPath().toLowerCase())).collect(Collectors.toCollection(ArrayList::new));
                            for(Delta.DeltaFileItem itremove : itemsToRemove){
                                removeEntry(totalEntry.getValue(), itremove);
                            }
                        }
                        totalDiff.get(entry.getKey()).add(item);
                    }
                }
            }
        }
        return totalDiff;
    }
    private void removeEntry(SortedSet<Delta.DeltaFileItem> totalEntry,Delta.DeltaFileItem itemToRemove){
        totalEntry.remove(itemToRemove);
    }

    public int createJsTreeFromWc(FileItem wc, ArrayList<JsTreeItem> jstree, String path, int parentId, Integer id){
        String fileName = "root";
        if(wc.getName() != null){
            fileName = wc.getName();
        }
        if(wc.getFileType() == FileType.FILE){
            JsTreeAttributes attr = new JsTreeAttributes(wc.getFileContent(),Paths.get(path,wc.getName()).toString());
            jstree.add(new JsTreeItem(id.toString(), Integer.toString(parentId), fileName,"jstree-file",attr));
            return id;
        }
        JsTreeAttributes attr = new JsTreeAttributes(wc.getFileContent(),fileName.equals("root") ? path : Paths.get(path,fileName).toString());
        jstree.add(new JsTreeItem(id.toString(),fileName.equals("root") ? "#" : Integer.toString(parentId), fileName,"jstree-folder",attr));
        parentId = id;
        for(FileItem item : ((Tree)wc).getFiles()){
            id++;
            id = createJsTreeFromWc(item,jstree,Paths.get(path,fileName).toString(),parentId, id);
        }
        return id;
    }

    public ArrayList<JsTreeItem> getJsTreeListByCommitSha1(String sha1) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        String pathToRepository = getRepository().getRepositoryPath().toString();
        Path pathToCommit = Paths.get(getRepository().getObjectsFolderPath().toString(), sha1);
        ArrayList<JsTreeItem> jstree = new ArrayList<>();
        Tree tree = WorkingCopyUtils.getWorkingCopyTreeFromCommit(Commit.createCommitInstanceByPath(pathToCommit), pathToRepository);
        createJsTreeFromWc(tree, jstree,pathToRepository,0,0);
        return jstree;
    }

    public ArrayList<JsTreeItem> getCurrentWorkingCopyJsTree(String userName) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        String pathToRepository = getRepository().getRepositoryPath().toString();
        ArrayList<JsTreeItem> jstree = new ArrayList<>();
        WorkingCopyUtils wcUtils = new WorkingCopyUtils(pathToRepository, userName,new Date());
        Tree wc = wcUtils.getWc();
        wc.setName(null);
        createJsTreeFromWc(wc, jstree ,pathToRepository,0,0);
        return jstree;
    }

}
