package com.magit.logic.system.managers;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.enums.FileType;
import com.magit.logic.enums.Resolve;
import com.magit.logic.exceptions.PreviousCommitsLimitExceededException;
import com.magit.logic.system.objects.*;
import com.magit.logic.utils.compare.Delta;
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
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.magit.logic.enums.Resolve.TheirsNewFile;

public class MergeEngine {
    private Repository repository;
    private String oursCommitSha1;
    private String theirCommitSha1;
    private String ancestorCommitSha1;
    public void merge(Repository repository, Branch branch) throws ParseException, PreviousCommitsLimitExceededException, IOException {
        this.repository = repository;
        String headSha1 = branch.getPointedCommitSha1().toString();

        String sha1OfAncestor = findAncestor(headSha1, "e28358e4abba57e16a93325b844f784aa013e214", repository);
        if (sha1OfAncestor.equals(""))
            return;

        String pathToObjectsFolder = repository.getObjectsFolderPath().toString();
        Commit oursCommit = Commit.createCommitInstanceByPath(Paths.get(pathToObjectsFolder, headSha1));
        Commit ancestorCommit = Commit.createCommitInstanceByPath(Paths.get(pathToObjectsFolder, sha1OfAncestor));
        Commit theirsCommit = Commit.createCommitInstanceByPath(Paths.get(pathToObjectsFolder, "e28358e4abba57e16a93325b844f784aa013e214"));

        if (null == oursCommit || null == ancestorCommit || null == theirsCommit)
            return; // is that the way it should be handled?? todo
        oursCommitSha1 = oursCommit.getSha1();
        theirCommitSha1 = theirsCommit.getSha1();
        ancestorCommitSha1 = ancestorCommit.getSha1();

        String repositoryPath = repository.getRepositoryPath().toString();

        SortedSet<Delta.DeltaFileItem> oursDelta = WorkingCopyUtils.getDeltaFileItemSetFromCommit(oursCommit, repositoryPath);
        SortedSet<Delta.DeltaFileItem> theirsDelta = WorkingCopyUtils.getDeltaFileItemSetFromCommit(theirsCommit, repositoryPath);
        SortedSet<Delta.DeltaFileItem> ancestorDelta = WorkingCopyUtils.getDeltaFileItemSetFromCommit(ancestorCommit, repositoryPath);
        //check fast forward merge
        SortedSet<Pair<String, MergeStateFileItem>> mergeItemsMap = getMergeState(oursDelta, theirsDelta, ancestorDelta);
        executeMerge(mergeItemsMap);
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
                    file.mkdirs();
                    FileHandler.writeNewFile(Paths.get(file.getAbsolutePath(), "merge-info").toString()
                            , String.format("ours:%s%s" +
                                            "theirs:%s%s" +
                                            "ancestor:%s",
                                    oursCommitSha1,System.lineSeparator()
                            , theirCommitSha1, System.lineSeparator()
                            ,ancestorCommitSha1));
                }
                handleMergeStateFileItem(pair.getValue(), status, pair.getKey());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                addFileToOpenChangesFile(pathToOpenChanges,location,FileStatus.REMOVED,fileItem.getOurs());
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
            FileHandler.appendFileWithContentAndLine(pathToConflicts,String.format("path:%s;%s", pathToFile.toLowerCase().replace(repository.getRepositoryPath().toString().toLowerCase(), ""),mergeStateFileItemileItem.toString()));
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
}
