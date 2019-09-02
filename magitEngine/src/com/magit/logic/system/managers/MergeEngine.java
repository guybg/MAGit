package com.magit.logic.system.managers;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.enums.FileType;
import com.magit.logic.enums.Resolve;
import com.magit.logic.exceptions.*;
import com.magit.logic.system.objects.*;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.utils.digest.Sha1;
import com.magit.logic.utils.file.FileHandler;
import com.magit.logic.utils.file.FileItemHandler;
import com.magit.logic.utils.file.WorkingCopyUtils;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import puk.team.course.magit.ancestor.finder.AncestorFinder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;

public class MergeEngine {
    private Repository repository;
    private String oursCommitSha1;
    private String theirCommitSha1;
    private String ancestorCommitSha1;
    private String theirsBranchName;
    public void merge(Repository repository, Branch branchToBeMergedWith, boolean pullOperation) throws ParseException, PreviousCommitsLimitExceededException, IOException, UnhandledMergeException, MergeNotNeededException, FastForwardException, MergeException {
        if(headBranchHasUnhandledMerge(repository)){
            throw new UnhandledMergeException("there is already unsolved merge at this branch, information loaded.");
        }
        if(branchToBeMergedWith.getIsRemote() && !pullOperation)
            throw new MergeException("Cannot merge branch with remote branch.");
        theirsBranchName = branchToBeMergedWith.getBranchName();
        this.repository = repository;

        String pathToObjectsFolder = repository.getObjectsFolderPath().toString();
        oursCommitSha1 = repository.getBranches().get("HEAD").getPointedCommitSha1().toString();
        Commit oursCommit = Commit.createCommitInstanceByPath(Paths.get(pathToObjectsFolder, oursCommitSha1));
        Commit theirsCommit = Commit.createCommitInstanceByPath(Paths.get(pathToObjectsFolder, branchToBeMergedWith.getPointedCommitSha1().toString()));
        theirCommitSha1 = theirsCommit.getSha1();
        if(isHeadHasNoCommit()){
            handleFastForward(theirsCommit);
        }
        String sha1OfAncestor = findAncestor(oursCommitSha1, branchToBeMergedWith.getPointedCommitSha1().toString(), repository);
        Commit ancestorCommit = Commit.createCommitInstanceByPath(Paths.get(pathToObjectsFolder, sha1OfAncestor));
        if (sha1OfAncestor.equals(""))
            return;
        ancestorCommitSha1 = ancestorCommit.getSha1();





        if (null == oursCommit || null == ancestorCommit || null == theirsCommit)
            return; // is that the way it should be handled?? todo


        String repositoryPath = repository.getRepositoryPath().toString();

        SortedSet<Delta.DeltaFileItem> oursDelta = WorkingCopyUtils.getDeltaFileItemSetFromCommit(oursCommit, repositoryPath);
        SortedSet<Delta.DeltaFileItem> theirsDelta = WorkingCopyUtils.getDeltaFileItemSetFromCommit(theirsCommit, repositoryPath);
        SortedSet<Delta.DeltaFileItem> ancestorDelta = WorkingCopyUtils.getDeltaFileItemSetFromCommit(ancestorCommit, repositoryPath);
        //check fast forward merge
        if(isFastForward()){
            handleFastForward(theirsCommit);
            //File file = new File(Paths.get(repository.getMagitFolderPath().toString(),".merge", repository.getBranches().get("HEAD").getBranchName()).toString());
            //createMergeInfoFile(file);
            //FileHandler.writeNewFile(Paths.get(file.getAbsolutePath(), "fast-forward").toString(),theirCommitSha1);
            //FileHandler.clearFolder(Paths.get(repositoryPath));
            //WorkingCopyUtils.unzipWorkingCopyFromCommit(theirsCommit,repositoryPath,repositoryPath);
            //throw new FastForwardException("Fast forward merge - please commit the operation.");
        }
        SortedSet<Pair<String, MergeStateFileItem>> mergeItemsMap = getMergeState(oursDelta, theirsDelta, ancestorDelta);
        executeMerge(mergeItemsMap);
        parseMergeFiles(repository);
    }

    private void handleFastForward(Commit theirsCommit) throws IOException, ParseException, FastForwardException {
        final String repositoryPath = repository.getRepositoryPath().toString();
        File file = new File(Paths.get(repository.getMagitFolderPath().toString(),".merge", repository.getBranches().get("HEAD").getBranchName()).toString());
        createMergeInfoFile(file);
        FileHandler.writeNewFile(Paths.get(file.getAbsolutePath(), "fast-forward").toString(),theirCommitSha1);
        FileHandler.clearFolder(Paths.get(repositoryPath));
        WorkingCopyUtils.unzipWorkingCopyFromCommit(theirsCommit,repositoryPath,repositoryPath);
        throw new FastForwardException("Fast forward merge - please commit the operation.");
    }

    private boolean isFastForward() throws MergeNotNeededException {
        if(theirCommitSha1.equals(ancestorCommitSha1) || oursCommitSha1.equals(theirCommitSha1) || theirCommitSha1.isEmpty()){
            throw new MergeNotNeededException("Merge not needed, last commit already contains another branch's commit");
        } else if(oursCommitSha1.equals(ancestorCommitSha1)){
            return true;
        }
        return false;
    }

    private boolean isHeadHasNoCommit(){
        if(oursCommitSha1.isEmpty())
            return true;
        return false;
    }

    private void executeMerge(SortedSet<Pair<String, MergeStateFileItem>> mergeStateItemsMap) {
        File file = new File(Paths.get(repository.getMagitFolderPath().toString(),".merge", repository.getBranches().get("HEAD").getBranchName()).toString());
        try {
            FileHandler.clearFolder(repository.getRepositoryPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Pair<String, MergeStateFileItem> pair : mergeStateItemsMap) {
            Resolve status;
            try {
                status = Resolve.resolve(pair.getValue());
                if(status != Resolve.UnChanged && !file.exists()){
                    createMergeInfoFile(file);
                }
                handleMergeStateFileItem(pair.getValue(), status, pair.getKey());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createMergeInfoFile(File file) throws IOException {
        file.mkdirs();
        String ancestorCommitSha1Code = "";
        if(ancestorCommitSha1 != null)
            ancestorCommitSha1Code = ancestorCommitSha1;

        FileHandler.writeNewFile(Paths.get(file.getAbsolutePath(), "merge-info").toString()
                , String.format("ours:%s%s" +
                                "theirs:%s%s" +
                                "ancestor:%s%s" +
                                "%s",
                        oursCommitSha1,System.lineSeparator()
                        , theirCommitSha1, System.lineSeparator()
                        ,ancestorCommitSha1Code,System.lineSeparator()
                        , theirsBranchName));
    }
    private void handleMergeStateFileItem(MergeStateFileItem fileItem,Resolve status, String location){
        String pathToBranchMergeFolder = Paths.get(repository.getMagitFolderPath().toString(),".merge", repository.getBranches().get("HEAD").getBranchName()).toString();
        String pathToConflicts = Paths.get(pathToBranchMergeFolder,"conflicts").toString();
        String pathToOpenChanges = Paths.get(pathToBranchMergeFolder, "open-changes").toString();
        String pathToObjects = repository.getObjectsFolderPath().toString();
        String locationWithoutFileName = new File(location).getParentFile().toString();
        if(fileItem.getOurs() != null && fileItem.getOurs().getFileType() == FileType.FOLDER) return;
        if(fileItem.getTheirs() != null && fileItem.getTheirs().getFileType() == FileType.FOLDER) return;
        if(fileItem.getAncestor() != null && fileItem.getAncestor().getFileType() == FileType.FOLDER) return;
        switch (status){
            case TheirsNewFile:
                try {
                    FileItemHandler.unzip(pathToObjects,fileItem.getTheirs().getSha1Code(),locationWithoutFileName,fileItem.getTheirs().getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                addFileToOpenChangesFile(pathToOpenChanges,location,FileStatus.NEW,fileItem.getTheirs());
                break;
            case TheirsEdited:
                try {
                    FileItemHandler.unzip(pathToObjects,fileItem.getTheirs().getSha1Code(),locationWithoutFileName,fileItem.getTheirs().getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                addFileToOpenChangesFile(pathToOpenChanges,location,FileStatus.EDITED,fileItem.getTheirs());
                break;
            case TheirsDeleted:
                addFileToOpenChangesFile(pathToOpenChanges,location,FileStatus.REMOVED,fileItem.getTheirs());
                break;
            case OursNewFile:
                try {
                    FileItemHandler.unzip(pathToObjects,fileItem.getOurs().getSha1Code(),locationWithoutFileName,fileItem.getOurs().getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                addFileToOpenChangesFile(pathToOpenChanges,location,FileStatus.NEW,fileItem.getOurs());
                break;
            case OursEdited:
                try {
                    FileItemHandler.unzip(pathToObjects,fileItem.getOurs().getSha1Code(),locationWithoutFileName,fileItem.getOurs().getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                addFileToOpenChangesFile(pathToOpenChanges,location,FileStatus.EDITED,fileItem.getOurs());
                break;
            case OursDeleted:
            case BothDeleted:
                addFileToOpenChangesFile(pathToOpenChanges,location,FileStatus.REMOVED,fileItem.getAncestor());
                break;
            case UnChanged:
                try {
                    FileItemHandler.unzip(pathToObjects,fileItem.getOurs().getSha1Code(),locationWithoutFileName,fileItem.getOurs().getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Conflict:
                addFileToConflictsFile(pathToConflicts,location,fileItem);
                break;
                default:

        }
    }

    private void addFileToOpenChangesFile(String pathToOpenChanges, String pathToFile, FileStatus status, FileItem fileItem){
        try {
            FileHandler.appendFileWithContentAndLine(pathToOpenChanges,String.format("%s;%s;%s", pathToFile.toLowerCase().replace(repository.getRepositoryPath().toString().toLowerCase(), ""),status
                    ,fileItem.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addFileToConflictsFile(String pathToConflicts, String pathToFile, MergeStateFileItem mergeStateFileItemileItem){
        try {
            FileHandler.appendFileWithContentAndLine(pathToConflicts,String.format("path==%s=/=%s", pathToFile.toLowerCase().replace(repository.getRepositoryPath().toString().toLowerCase(), ""),mergeStateFileItemileItem.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private SortedSet<Pair<String, MergeStateFileItem>> getMergeState(SortedSet<Delta.DeltaFileItem> oursDelta, SortedSet<Delta.DeltaFileItem> theirsDelta,
                                                                      SortedSet<Delta.DeltaFileItem> ancestorDelta) {
        SortedSet<Pair<String, MergeStateFileItem>> mergeStates = new TreeSet<>(Comparator.comparing(Pair::getKey));
        SortedSet<String> pathsOfFiles = new TreeSet<>();
        pathsOfFiles.addAll(setPaths(oursDelta));
        pathsOfFiles.addAll(setPaths(theirsDelta));
        pathsOfFiles.addAll((setPaths(ancestorDelta)));
        Map<String, Pair<FileStatus, Delta.DeltaFileItem>> oursTheirs = Delta.getDiffrencesByPath(oursDelta, theirsDelta);
        Map<String, Pair<FileStatus, Delta.DeltaFileItem>>  theirsAncestor = Delta.getDiffrencesByPath(theirsDelta, ancestorDelta);
        Map<String, Pair<FileStatus, Delta.DeltaFileItem>>  ancestorOurs = Delta.getDiffrencesByPath(oursDelta, ancestorDelta);

        for (String path : pathsOfFiles) {
            FileItem ours, theirs, ancestor;
            Delta.DeltaFileItem temp = oursDelta.stream().filter(i -> i.getFullPath().equals(path)).findFirst().orElse(null);
            ours = null == temp ? null : temp.getFileItem();
            temp = theirsDelta.stream().filter(i -> i.getFullPath().equals(path)).findFirst().orElse(null);
            theirs = null == temp ? null : temp.getFileItem();
            temp = ancestorDelta.stream().filter(i -> i.getFullPath().equals(path)).findFirst().orElse(null);
            ancestor = null == temp ? null : temp.getFileItem();
            FileStatus oursTheirsStatus = oursTheirs.containsKey(path) ? oursTheirs.get(path).getKey() : FileStatus.UNCHANGED;
            FileStatus theirsAncestorStatus = theirsAncestor.containsKey(path) ? theirsAncestor.get(path).getKey() : FileStatus.UNCHANGED;
            FileStatus ancestorOursStatus = ancestorOurs.containsKey(path) ? ancestorOurs.get(path).getKey() : FileStatus.UNCHANGED;
            MergeStateFileItem currentItem = new MergeStateFileItem(ours, theirs, ancestor, oursTheirsStatus, theirsAncestorStatus, ancestorOursStatus);
            mergeStates.add(new Pair<>(path, currentItem));
        }

        return mergeStates;
    }

    private TreeSet<String> setPaths(SortedSet<Delta.DeltaFileItem> delta) {
        TreeSet<String> treeSet = new TreeSet<>();
        for (Delta.DeltaFileItem item : delta) {
            treeSet.add(item.getFullPath());
        }
        return treeSet;
    }

    private String findAncestor(String headSha1, String sha1OfBranchToMerge, Repository repository) {
        AncestorFinder ancestorFinder = new AncestorFinder(sha1 -> {
            String pathToObjectFolder = repository.getObjectsFolderPath().toString();
            Path pathToCommit = Paths.get(pathToObjectFolder, sha1);
            try {
                return Commit.createCommitInstanceByPath(pathToCommit);
            } catch (IOException | ParseException | PreviousCommitsLimitExceededException e) {
                e.printStackTrace();
            }
            return null;
        });

        return ancestorFinder.traceAncestor(headSha1, sha1OfBranchToMerge);
    }

    public HashMap<FileStatus, ArrayList<FileItemInfo>> getOpenChanges(Repository repository){
        return parseOpenChanges(repository);
    }
    private void parseMergeFiles(Repository repository) {
        HashMap<FileStatus, ArrayList<FileItemInfo>> openChangesMap = parseOpenChanges(repository);
        ArrayList<ConflictItem> conflictItems = parseConflictsFile(repository);

    }

    public ArrayList<ConflictItem> getConflictItems(Repository repository){
        return parseConflictsFile(repository);
    }

    private HashMap<FileStatus, ArrayList<FileItemInfo>> parseOpenChanges(Repository repository) {
        final int path = 0, state = 1, name = 2, sha1 = 3, createdBy = 5, date = 6;
        ArrayList<FileItemInfo> editedFiles = new ArrayList<>();
        ArrayList<FileItemInfo> deletedFiles = new ArrayList<>();
        ArrayList<FileItemInfo> newFiles = new ArrayList<>();
        String branchName = repository.getBranches().get("HEAD").getBranchName();
        Path pathToOpenChanges = Paths.get(repository.getMagitFolderPath().toString(), ".merge", branchName, "open-changes");

        if (Files.notExists(pathToOpenChanges))
            return createMergeDiffMap(editedFiles,deletedFiles,newFiles);

        String filesContent = "";
        try {
            filesContent = FileHandler.readFile(pathToOpenChanges.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] linesOfOpenChanges = filesContent.split(System.lineSeparator());
        for (String line : linesOfOpenChanges) {
            String[] fieldsOfFileItemInfo = line.split(";");
            Path fullPath = Paths.get(repository.getRepositoryPath().toString(), fieldsOfFileItemInfo[path]);
            switch (FileStatus.valueOf(fieldsOfFileItemInfo[state])){
                case EDITED:
                    editedFiles.add(createFileItemInfoWrapper(repository, name, sha1, createdBy, date, fullPath.toString(), fieldsOfFileItemInfo));
                    break;
                case REMOVED:
                    deletedFiles.add(createFileItemInfoWrapper(repository, name, sha1, createdBy, date, fullPath.toString(), fieldsOfFileItemInfo));
                    break;
                case NEW:
                    newFiles.add(createFileItemInfoWrapper(repository, name, sha1, createdBy, date, fullPath.toString(), fieldsOfFileItemInfo));
                    break;
            }
        }
        return createMergeDiffMap(editedFiles,deletedFiles,newFiles);
    }

    private HashMap<FileStatus, ArrayList<FileItemInfo>> createMergeDiffMap(ArrayList<FileItemInfo> editedFiles,ArrayList<FileItemInfo> deletedFiles,ArrayList<FileItemInfo> newFiles){
        HashMap<FileStatus, ArrayList<FileItemInfo>> openChangesMap = new HashMap<>();
        openChangesMap.put(FileStatus.EDITED, editedFiles);
        openChangesMap.put(FileStatus.REMOVED, deletedFiles);
        openChangesMap.put(FileStatus.NEW, newFiles);
        return openChangesMap;
    }

    private ArrayList<ConflictItem> parseConflictsFile(Repository repository) {
        final int name = 0, sha1 = 1, updatedBy = 3, date = 4,
                pathIndex = 0, oursIndex = 1, theirsIndex = 2, ancestorIndex =3;
        ArrayList<ConflictItem> conflictItems = new ArrayList<>();
        String branchName = repository.getBranches().get("HEAD").getBranchName();
        Path pathToConflictsFiles = Paths.get(repository.getMagitFolderPath().toString(), ".merge", branchName, "conflicts");

        if (Files.notExists(pathToConflictsFiles))
            return null; //throw

        String fileContent = "";

        try {
            fileContent = FileHandler.readFile(pathToConflictsFiles.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] linesOfFile = fileContent.split(System.lineSeparator());
        for (String line : linesOfFile) {
            String[] splitter = line.split("=/=");
            String path = splitter[pathIndex].split("==")[1];
            String ours = splitter[oursIndex];
            String theirs = splitter[theirsIndex];
            String ancestor = splitter[ancestorIndex];
            ConflictItem conflictItem = new ConflictItem(Paths.get(repository.getRepositoryPath().toString(),path).toString());

            for (String fileItemString : new String[]{ours, theirs, ancestor}) {
                String[] nameInfo = fileItemString.split("==");
                String commitSide = nameInfo[0];
                String[] info = nameInfo[1].split(";");
                if (info[0].equals("none"))
                    continue;

                FileItemInfo itemInfo = createFileItemInfoWrapper(repository, name, sha1, updatedBy, date, path, info);
                conflictItem.setFileName(info[name]);
                conflictItem.set(commitSide, itemInfo);
            }
            conflictItems.add(conflictItem);
        }
        return conflictItems;
    }

    private FileItemInfo createFileItemInfoWrapper(Repository repository, int name, int sha1, int updatedBy, int date, String path, String[] info) {
        String content = "";
        try {
            content = FileItemHandler.zipToString(repository.getObjectsFolderPath().toString(), new Sha1(info[sha1], true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FileItemInfo(info[name], "FILE", info[sha1],
                info[updatedBy], info[date], content, path);
    }

    public void saveSolvedConflictItem(String path, String fileContent, Repository repository, boolean deleted){
        try {String conflictsPath = Paths.get(repository.getMagitFolderPath().toString(),".merge", repository.getBranches().get("HEAD").getBranchName(), "conflicts").toString();
            String conflicts = FileHandler.readFile(conflictsPath);
            StringBuilder updatedConflicts = new StringBuilder();
            for(String line : conflicts.split(System.lineSeparator())){
                if(!line.contains(path.toLowerCase().replace(repository.getRepositoryPath().toString().toLowerCase(), ""))){
                    updatedConflicts.append(String.format("%s%s",line, System.lineSeparator()));
                }
            }
            FileHandler.writeNewFile(conflictsPath, updatedConflicts.toString());
            if(!deleted)
                FileHandler.writeNewFile(path, fileContent);
            if(updatedConflicts.toString().isEmpty()){
                FileUtils.deleteQuietly(new File(conflictsPath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean headBranchHasUnhandledMerge(Repository repository){
        return Files.exists(Paths.get(repository.getMagitFolderPath().toString(),".merge",repository.getBranches().get("HEAD").getBranchName()));
    }

    public boolean headBranchHasMergeConflicts(Repository repository){
        return Files.exists(Paths.get(repository.getMagitFolderPath().toString(),".merge",repository.getBranches().get("HEAD").getBranchName(), "conflicts"));
    }
    public boolean headBranchHasMergeOpenChanges(Repository repository){
        return Files.exists(Paths.get(repository.getMagitFolderPath().toString(),".merge",repository.getBranches().get("HEAD").getBranchName(), "open-changes"));
    }
}
