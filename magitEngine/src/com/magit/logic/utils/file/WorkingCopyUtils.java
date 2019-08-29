package com.magit.logic.utils.file;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.CommitNotFoundException;
import com.magit.logic.exceptions.PreviousCommitsLimitExceededException;
import com.magit.logic.exceptions.WorkingCopyIsEmptyException;
import com.magit.logic.system.interfaces.WalkAction;
import com.magit.logic.system.objects.*;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.utils.digest.Sha1;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import sun.plugin.javascript.navig.Array;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WorkingCopyUtils {
    private static final Predicate<FileItem> treePredicate = fileItem -> fileItem.getFileType() == FileType.FOLDER;
    private static final Predicate<FileItem> blobPredicate = fileItem -> fileItem.getFileType() == FileType.FILE;
    private final String mRepositoryDirectoryPath;
    private final String mUserName;
    private final Date mCommitDate;

    public WorkingCopyUtils(String repositoryDirectoryPath, String userName, Date commitDate) {
        mRepositoryDirectoryPath = repositoryDirectoryPath;
        mUserName = userName;
        mCommitDate = commitDate;
    }

    private static FileItem walk(Sha1 sha1Code, FileType mFileType, String mLastUpdater, Date mCommitDate,
                                 String mName, String repositoryPath, String filePath, SortedSet<Delta.DeltaFileItem> deltaFileItems) throws IOException, ParseException {
        if (mFileType == FileType.FOLDER) {
            SortedSet<FileItem> files = new TreeSet<>();
            ArrayList<String[]> fileItems = Tree.treeItemsToStringArray(FileItemHandler.zipToString(Paths.get(repositoryPath, ".magit", "objects").toString(), sha1Code));
            for (String[] fileItem : fileItems) {
                if (fileItem.length < 4) continue;
                DateFormat formatter1;
                formatter1 = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");
                Date date = formatter1.parse(fileItem[4]);
                files.add(walk(new Sha1(fileItem[1], true), FileType.valueOf(fileItem[2]), fileItem[3], date, fileItem[0], repositoryPath, Paths.get(filePath, fileItem[0]).toString(), deltaFileItems));
            }
            Tree tree = new Tree(mName, sha1Code, FileType.FOLDER, mLastUpdater, mCommitDate, files);
            addToDeltaSet(filePath, deltaFileItems, tree);
            return tree;
        } else {
            String fileContent = FileItemHandler.zipToString(Paths.get(repositoryPath, ".magit", "objects").toString(), sha1Code);
            Blob blob = new Blob(mName, fileContent, mFileType, mLastUpdater, mCommitDate);
            addToDeltaSet(filePath, deltaFileItems, blob);
            return blob;
        }
    }

    public static Tree getWorkingCopyTreeFromCommit(Commit commit, String repositoryPath) throws IOException, ParseException {
        if (commit == null)
            return null;
        Sha1 wcSha1 = commit.getWorkingCopySha1();
        return (Tree) walk(wcSha1, FileType.FOLDER, commit.getLastUpdater(), commit.getLastModified(), commit.getName(), repositoryPath, repositoryPath, null);
    }

    public static SortedSet<Delta.DeltaFileItem> getDeltaFileItemSetFromCommit(Commit commit, String repositoryPath) throws IOException, ParseException {
        if (commit == null)
            return null;
        Sha1 wcSha1 = commit.getWorkingCopySha1();
        SortedSet<Delta.DeltaFileItem> deltaFiles = new TreeSet<>();
        walk(wcSha1, FileType.FOLDER, commit.getLastUpdater(), commit.getLastModified(), commit.getName(), repositoryPath, repositoryPath, deltaFiles);
        return deltaFiles;
    }

    public static Map<FileStatus, SortedSet<Delta.DeltaFileItem>> getDifferencesBetweenCurrentWcAndLastCommit(
            SortedSet<Delta.DeltaFileItem> curWcDeltaFiles,
            SortedSet<Delta.DeltaFileItem> commitDeltaFiles) {
        return Delta.getDifferences(curWcDeltaFiles, commitDeltaFiles);
    }

    private static void addToDeltaSet(String filePath, SortedSet<Delta.DeltaFileItem> deltaFileItems, FileItem file) {
        if (deltaFileItems != null && file.getName() != null) {
            deltaFileItems.add(new Delta.DeltaFileItem(file, filePath));
        }
    }

    public static void unzipWorkingCopyFromCommit(Commit commit, String destinationPath, String repositoryPath) throws IOException, ParseException {
        Tree wc = getWorkingCopyTreeFromCommit(commit, repositoryPath);
        WalkAction walkAction = new WalkAction() {
            @Override
            public void onWalkAction(FileItem file, Object... params) throws IOException {
                if (file.getFileType() == FileType.FILE)
                    FileItemHandler.fileItemToFile((Blob) file, (String) params[0], ((String) params[1]));
                else
                    FileItemHandler.fileItemToFile((String) params[0], ((String) params[1]));
            }

            @Override
            public void onAddAction(SortedSet set, SortedSet dirFiles, FileItem fileIte, String filePath) {
            }
        };
        fileItemWalk(wc, destinationPath, walkAction);
    }

    public static void updateNewObjects(Repository source, Repository destination) throws IOException, ParseException, PreviousCommitsLimitExceededException, CommitNotFoundException {
        for(String sha1OfCommit : source.getAllCommitsOfRepository()){
            if(!Arrays.asList(destination.getAllCommitsOfRepository()).contains(sha1OfCommit)){
                Commit commit = Commit.createCommitInstanceByPath(Paths.get(source.getObjectsFolderPath().toString(),sha1OfCommit));
                if(commit == null)
                    throw new CommitNotFoundException("commit not found, repository corrupted");
                FileHandler.appendFileWithContentAndLine(Paths.get(destination.getMagitFolderPath().toString(),"COMMITS").toString(), sha1OfCommit);
                commit.generateCommitFile(destination.getObjectsFolderPath());
                Tree wc = getWorkingCopyTreeFromCommit(commit,source.getRepositoryPath().toString());
                fileItemWalk(wc, destination.getObjectsFolderPath().toString(), new WalkAction() {
                    @Override
                    public void onWalkAction(FileItem file, Object... params) throws IOException {
                        for(File objectsFile : Objects.requireNonNull(destination.getObjectsFolderPath().toFile().listFiles())){
                            if(objectsFile.getName().equals(file.getSha1Code().toString())) return;
                        }
                        FileItemHandler.zip(file, destination.getObjectsFolderPath().toString());
                    }

                    @Override
                    public void onAddAction(SortedSet set, SortedSet dirFiles, FileItem fileItem, String filePath) {
                    }
                });
            }
        }
    }

    private static void fileItemWalk(FileItem fileItem, String destinationPath, WalkAction aAction) throws IOException {
        if (fileItem.getFileType() == FileType.FILE || ((Tree) fileItem).getNumberOfFiles() == 0) {
            if (fileItem.getFileType() == FileType.FILE) {
                aAction.onWalkAction(fileItem, destinationPath, fileItem.getName());
            }
            return;
        }
        if (fileItem.getName() != null) {
            aAction.onWalkAction(fileItem, destinationPath, fileItem.getName());
            destinationPath = Paths.get(destinationPath, fileItem.getName()).toString();
        }
        for (FileItem file : ((Tree) fileItem).getFiles()) {
            fileItemWalk(file, Paths.get(destinationPath).toString(), aAction);
        }

    }

    public static String getWorkingCopyContent(Tree workingCopy, String repositoryDirectoryPath, String userName) {
        return toPrintFormat(workingCopy, repositoryDirectoryPath, userName);
    }

    private static FileItem updateWalk(Tree newWc, Tree oldWc, Tree wc) {
        SortedSet<FileItem> tr1 = new TreeSet<>(CollectionUtils.select(newWc.getFiles(), treePredicate));
        SortedSet<FileItem> bl1 = new TreeSet<>(CollectionUtils.select(newWc.getFiles(), blobPredicate));
        SortedSet<FileItem> tr2 = new TreeSet<>(CollectionUtils.select(oldWc.getFiles(), treePredicate));
        SortedSet<FileItem> bl2 = new TreeSet<>(CollectionUtils.select(oldWc.getFiles(), blobPredicate));
        SortedSet<FileItem> dif = new TreeSet<>(CollectionUtils.union(CollectionUtils.intersection(bl2, bl1), bl1));
        wc.setFiles(dif);
        if (tr1.isEmpty()) {
            if (!bl1.isEmpty()) {
                return wc;
            }
        }
        for (FileItem tree : tr1) {
            boolean isNewFolder = true;
            for (FileItem tree2 : tr2) {
                if (tree2.getName().equals(tree.getName())) {
                    String updater = tree.getLastUpdater();
                    Date date = tree.getLastModified();
                    if (CollectionUtils.isEqualCollection(((Tree) tree).getFiles(), ((Tree) tree2).getFiles())) {
                        updater = tree2.getLastUpdater();
                        date = tree2.getLastModified();
                    }
                    wc.addFileItem(updateWalk((Tree) tree, (Tree) tree2, new Tree(tree.getFileType(), updater, date, tree.getName(), new TreeSet<>())));
                    isNewFolder = false;
                }
            }
            if (isNewFolder) {
                wc.addFileItem(tree);
            }
        }
        return wc;
    }

    public static Tree getWcWithOnlyNewChanges(Tree newWc, Tree oldWc) {
        Tree wc;
        if (CollectionUtils.isEqualCollection(newWc.getFiles(), oldWc.getFiles()))
            wc = oldWc;
        else
            wc = (Tree) updateWalk(newWc, oldWc, new Tree(FileType.FOLDER, newWc.getLastUpdater(), newWc.getLastModified(), newWc.getName(), new TreeSet<>()));
        return wc;
    }
    static ObservableList<FileItemInfo> list;
    public static ObservableList<FileItemInfo> guiGetWorkingCopyContent(Tree workingCopyFolder, String repositoryDirectoryPath, String userName){
        ObservableList<FileItemInfo> files = FXCollections.observableArrayList();
        guiWorkingCopyFilesToArrayList(workingCopyFolder, repositoryDirectoryPath, userName,files);
        return files;
    }
    private static void guiWorkingCopyFilesToArrayList(FileItem item, String repositoryDirectoryPath, String userName, ObservableList<FileItemInfo> files) {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");
        String fileName;
        String fileContent = "";
        if(item.getName() == null){
            fileName = "Root folder";
        }else{
            fileName = item.getName();
        }
        if(item.getFileType().equals(FileType.FILE)) fileContent = item.getFileContent();
        FileItemInfo file = new FileItemInfo(fileName, item.getFileType().toString(), item.getSha1Code().toString(), item.getLastUpdater(), dateFormat.format(item.getLastModified()),fileContent,repositoryDirectoryPath);
        files.add(file);
        if(item.getFileType().equals(FileType.FOLDER)) {
            for (FileItem fileToAdd : ((Tree)item).listFiles()) {
                guiWorkingCopyFilesToArrayList(fileToAdd, Paths.get(repositoryDirectoryPath, fileToAdd.getName()).toString(),userName,files);
            }
        }
    }

    private static String toPrintFormat(Tree workingCopyFolder, String repositoryDirectoryPath, String userName) {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");
        StringBuilder workingCopyContent = new StringBuilder();

        workingCopyContent.append(String.format("Files Information ( . == [%s] )%s",
                repositoryDirectoryPath, System.lineSeparator()));
        workingCopyContent.append(String.format("==============================================%s", System.lineSeparator()));
        workingCopyContent.append(String.format("[Root Folder] --> .%s", System.lineSeparator()));
        workingCopyContent.append(String.format("%s%s", workingCopyFolder.getSha1Code(), System.lineSeparator()));
        workingCopyContent.append(String.format("WC - %s%s", dateFormat.format(workingCopyFolder.getLastModified()), System.lineSeparator()));
        workingCopyContent.append(String.format("Last modifier: %s%s", workingCopyFolder.getLastUpdater(), System.lineSeparator()));
        workingCopyContent.append(String.format("==============================================%s", System.lineSeparator()));
        for (FileItem fileToPrint : workingCopyFolder.listFiles()) {
            workingCopyContent.append(workingCopyToPrint(fileToPrint, Paths.get(".")));
        }

        workingCopyContent.append(
                String.format("Current User: %s      Current Repository Location: %s %s",
                        userName, repositoryDirectoryPath, System.lineSeparator()));

        return workingCopyContent.toString();
    }

    private static String workingCopyToPrint(FileItem fileToPrint, Path pathToFile) {
        StringBuilder contentToPrint = new StringBuilder();
        Path pathOfFile = Paths.get(pathToFile.toString(), fileToPrint.getName());
        if (fileToPrint.getFileType() == FileType.FILE) {
            contentToPrint.append(fileToPrint.toPrintFormat(pathOfFile.toString()));
            return contentToPrint.toString();
        }

        contentToPrint.append(fileToPrint.toPrintFormat(pathOfFile.toString()));
        for (FileItem fileInDirectory : ((Tree) fileToPrint).listFiles()) {
            contentToPrint.append(workingCopyToPrint(fileInDirectory, pathOfFile));
        }

        return contentToPrint.toString();
    }

    public void zipWorkingCopyFromTreeWC(Tree wc) throws IOException {
        WalkAction walkAction = new WalkAction() {
            @Override
            public void onWalkAction(FileItem file, Object... params) throws IOException {
                FileItemHandler.zip(file, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
            }

            @Override
            public void onAddAction(SortedSet set, SortedSet dirFiles, FileItem fileItem, String filePath) {

            }
        };
        fileItemWalk(wc, mRepositoryDirectoryPath, walkAction);

    }

    public Tree getWc() throws IOException {
        SortedSet<FileItem> directoryFiles = new TreeSet<>();
        WalkAction<FileItem> walkAction = new WalkAction<FileItem>() {
            @Override
            public void onWalkAction(FileItem file, Object... params) {

            }

            @Override
            public void onAddAction(SortedSet<FileItem> set, SortedSet<FileItem> dirFiles, FileItem fileItem, String filePath) {
                dirFiles.add(fileItem);
            }

        };
        wcWalk(mRepositoryDirectoryPath, null, directoryFiles, walkAction);
        return new Tree(FileType.FOLDER, mUserName, mCommitDate, "wc", directoryFiles);
    }

    public void clearWorkingCopyFiles(Path repositoryPath) throws IOException { ///@@@@@@@@@@@@????????????????????? TODO (REMOVE THIS?)
        FileHandler.clearFolder(repositoryPath);
    }

    public Sha1 zipWorkingCopyFromCurrentWorkingCopy() throws IOException, WorkingCopyIsEmptyException {
        SortedSet<FileItem> directoryFiles = new TreeSet<>();
        WalkAction action = new WalkAction() {
            @Override
            public void onWalkAction(FileItem file, Object... params) throws IOException {
                FileItemHandler.zip(file, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
            }

            @Override
            public void onAddAction(SortedSet set, SortedSet dirFiles, FileItem fileItem, String filePath) {
                dirFiles.add(fileItem);
            }
        };
        wcWalk(mRepositoryDirectoryPath, null, directoryFiles, action);
        Tree wc = new Tree(FileType.FOLDER, mUserName, mCommitDate, "wc", directoryFiles);
        if (wc.getFiles().isEmpty()) {
            throw new WorkingCopyIsEmptyException();
        }
        FileItemHandler.zip(wc, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
        return wc.getSha1Code();
    }

    public SortedSet<Delta.DeltaFileItem> getAllDeltaFilesFromCurrentWc() throws IOException {
        SortedSet<Delta.DeltaFileItem> deltaFiles = new TreeSet<>();
        WalkAction<Delta.DeltaFileItem> walkAction = new WalkAction<Delta.DeltaFileItem>() {
            @Override
            public void onWalkAction(FileItem file, Object... params) {

            }

            @Override
            public void onAddAction(SortedSet<Delta.DeltaFileItem> set, SortedSet<FileItem> dirFiles, FileItem fileItem, String filePath) {
                set.add(new Delta.DeltaFileItem(fileItem, filePath));
                dirFiles.add(fileItem);
            }
        };
        wcWalk(mRepositoryDirectoryPath, deltaFiles, new TreeSet<>(), walkAction);
        return deltaFiles;
    }

    private <T> void wcWalk(String repositoryDirectoryPath, SortedSet<T> set, SortedSet<FileItem> directoryFiles, WalkAction<T> wAction) throws IOException {
        File root = new File(repositoryDirectoryPath);
        File[] list = root.listFiles();
        if (list == null) return;

        for (File f : list) {
            if (!f.getName().equals(".magit")) {
                if (f.isDirectory()) {
                    if (Objects.requireNonNull(f.listFiles()).length == 0) continue;
                    SortedSet<FileItem> dirFiles = new TreeSet<>();
                    wcWalk(f.getAbsolutePath(), set, dirFiles, wAction);

                    Tree tree = new Tree(FileType.FOLDER, mUserName, mCommitDate, f.getName(), dirFiles);

                    wAction.onAddAction(set, directoryFiles, tree, f.getAbsolutePath());
                    wAction.onWalkAction(tree, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
                } else {
                    Blob blob = new Blob(f.getName(), FileHandler.readFile(f.getAbsolutePath()), FileType.FILE, mUserName, mCommitDate);

                    wAction.onAddAction(set, directoryFiles, blob, f.getAbsolutePath());
                    wAction.onWalkAction(blob, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
                }
            }
        }
    }
}