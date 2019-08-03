package com.magit.logic.utils.file;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.WorkingCopyIsEmptyException;
import com.magit.logic.system.interfaces.WalkAction;
import com.magit.logic.system.objects.Blob;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.FileItem;
import com.magit.logic.system.objects.Tree;
import com.magit.logic.utils.compare.Delta;
import com.magit.logic.utils.digest.Sha1;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WorkingCopyUtils {
    private String mRepositoryDirectoryPath;
    private String mUserName;
    private Date mCommitDate;
    private static Predicate<FileItem> treePredicate = fileItem -> fileItem.getmFileType() == FileType.FOLDER;
    private static Predicate<FileItem> blobPredicate = fileItem -> fileItem.getmFileType() == FileType.FILE;

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
                formatter1 = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:SSS");
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
        Sha1 wcSha1 = commit.getmWorkingCopySha1();
        return (Tree) walk(wcSha1, FileType.FOLDER, commit.getmLastUpdater(), commit.getLastModified(), commit.getmName(), repositoryPath, repositoryPath, null);
    }

    public static SortedSet<Delta.DeltaFileItem> getDeltaFileItemSetFromCommit(Commit commit, String repositoryPath) throws IOException, ParseException {
        if (commit == null)
            return null;
        Sha1 wcSha1 = commit.getmWorkingCopySha1();
        SortedSet<Delta.DeltaFileItem> deltaFiles = new TreeSet<>();
        walk(wcSha1, FileType.FOLDER, commit.getmLastUpdater(), commit.getLastModified(), commit.getmName(), repositoryPath, repositoryPath, deltaFiles);
        return deltaFiles;
    }

    public static Map<FileStatus, SortedSet<Delta.DeltaFileItem>> getDifferencesBetweenCurrentWcAndLastCommit(
            SortedSet<Delta.DeltaFileItem> curWcDeltaFiles,
            SortedSet<Delta.DeltaFileItem> commitDeltaFiles) {
        return Delta.getDifferences(curWcDeltaFiles, commitDeltaFiles);
    }

    private static void addToDeltaSet(String filePath, SortedSet<Delta.DeltaFileItem> deltaFileItems, FileItem file) {
        if (deltaFileItems != null && file.getmName() != null) {
            deltaFileItems.add(new Delta.DeltaFileItem(file, filePath));
        }
    }

    public static void unzipWorkingCopyFromCommit(Commit commit, String destinationPath, String repositoryPath) throws IOException, ParseException {
        Tree wc = getWorkingCopyTreeFromCommit(commit, repositoryPath);
        WalkAction walkAction = new WalkAction() {
            @Override
            public void onWalkAction(FileItem file, Object... params) throws IOException {
                if (file.getmFileType() == FileType.FILE)
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

    private static void fileItemWalk(FileItem fileItem, String destinationPath, WalkAction aAction) throws IOException {
        if (fileItem.getmFileType() == FileType.FILE || ((Tree) fileItem).getNumberOfFiles() == 0) {
            if (fileItem.getmFileType() == FileType.FILE) {
                aAction.onWalkAction(fileItem, destinationPath, fileItem.getmName());
            }
            return;
        }
        if (fileItem.getmName() != null) {
            aAction.onWalkAction(fileItem, destinationPath, fileItem.getmName());
            destinationPath = Paths.get(destinationPath, fileItem.getmName()).toString();
        }
        for (FileItem file : ((Tree) fileItem).getmFiles()) {
            fileItemWalk(file, Paths.get(destinationPath).toString(), aAction);
        }

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

    public static String getWorkingCopyContent(Tree workingCopy, String repositoryDirectoryPath, String userName) {
        return toPrintFormat(workingCopy, repositoryDirectoryPath, userName);
    }

    private static FileItem updateWalk(Tree newWc, Tree oldWc, Tree wc) {
        SortedSet<FileItem> tr1 = new TreeSet<>(CollectionUtils.select(newWc.getmFiles(), treePredicate));
        SortedSet<FileItem> bl1 = new TreeSet<>(CollectionUtils.select(newWc.getmFiles(), blobPredicate));
        SortedSet<FileItem> tr2 = new TreeSet<>(CollectionUtils.select(oldWc.getmFiles(), treePredicate));
        SortedSet<FileItem> bl2 = new TreeSet<>(CollectionUtils.select(oldWc.getmFiles(), blobPredicate));
        SortedSet<FileItem> dif = new TreeSet<>(CollectionUtils.union(CollectionUtils.intersection(bl2, bl1), bl1));
        wc.setmFiles(dif);
        if (tr1.isEmpty()) {
            if (!bl1.isEmpty()) {
                return wc;
            }
        }
        for (FileItem tree : tr1) {
            boolean isNewFolder = true;
            for (FileItem tree2 : tr2) {
                if (tree2.getmName().equals(tree.getmName())) {
                    String updatater = tree.getmLastUpdater();
                    Date date = tree.getLastModified();
                    if (CollectionUtils.isEqualCollection(((Tree) tree).getmFiles(), ((Tree) tree2).getmFiles())) {
                        updatater = tree2.getmLastUpdater();
                        date = tree2.getLastModified();
                    }
                    wc.addFileItem(updateWalk((Tree) tree, (Tree) tree2, new Tree(tree.getmFileType(), updatater, date, tree.getmName(), new TreeSet<FileItem>())));
                    isNewFolder = false;
                }
            }
            if (isNewFolder) {
                wc.addFileItem(tree);
            }
        }
        return wc;
    }

    public static Tree getWcWithOnlyNewchanges(Tree newWc, Tree oldWc) {
        Tree wc;
        if (CollectionUtils.isEqualCollection(newWc.getmFiles(), oldWc.getmFiles()))
            wc = oldWc;
        else
            wc = (Tree) updateWalk(newWc, oldWc, new Tree(FileType.FOLDER, newWc.getmLastUpdater(), newWc.getLastModified(), newWc.getmName(), new TreeSet<>()));
        return wc;
    }

    private static String toPrintFormat(Tree workingCopyFolder, String repositoryDirectoryPath, String userName) {
        StringBuilder workingCopyContent = new StringBuilder();

        workingCopyContent.append(String.format("Files Information ( . == [%s] )%s",
                repositoryDirectoryPath, System.lineSeparator()));
        workingCopyContent.append(String.format("==============================================%s", System.lineSeparator()));
        workingCopyContent.append(String.format("[Root Folder] --> .%s", System.lineSeparator()));
        workingCopyContent.append(String.format("%s%s", workingCopyFolder.getSha1Code(), System.lineSeparator()));
        workingCopyContent.append(String.format("WC - %s%s", workingCopyFolder.getLastModified(), System.lineSeparator()));
        workingCopyContent.append(String.format("Last modifier: %s%s", workingCopyFolder.getmLastUpdater(), System.lineSeparator()));
        workingCopyContent.append(String.format("==============================================%s", System.lineSeparator()));
        for (FileItem fileToPrint : workingCopyFolder.listFiles()) {
            workingCopyContent.append(workingCopyToPrint(fileToPrint, Paths.get(".")));
        }

        workingCopyContent.append(
                String.format("Current User: %s      Current Repository Location: %s %s",
                        userName, repositoryDirectoryPath, System.lineSeparator()));

        return workingCopyContent.toString();
    }

    public Tree getWc() throws IOException {
        SortedSet<FileItem> directoryFiles = new TreeSet<>();
        WalkAction<FileItem> walkAction = new WalkAction<FileItem>() {
            @Override
            public void onWalkAction(FileItem file, Object... params) throws IOException {

            }

            @Override
            public void onAddAction(SortedSet<FileItem> set, SortedSet<FileItem> dirFiles, FileItem fileItem, String filePath) {
                dirFiles.add(fileItem);
            }

        };
        wcWalk(mRepositoryDirectoryPath, null, directoryFiles, walkAction);
        return new Tree(FileType.FOLDER, mUserName, mCommitDate, "wc", directoryFiles);
    }

    private static String workingCopyToPrint(FileItem fileToPrint, Path pathToFile) {
        StringBuilder contentToPrint = new StringBuilder();
        Path pathOfFile = Paths.get(pathToFile.toString(), fileToPrint.getmName());
        if (fileToPrint.getmFileType() == FileType.FILE) {
            contentToPrint.append(fileToPrint.toPrintFormat(pathOfFile.toString()));
            return contentToPrint.toString();
        }

        contentToPrint.append(fileToPrint.toPrintFormat(pathOfFile.toString()));
        for (FileItem fileInDirectory : ((Tree) fileToPrint).listFiles()) {
            contentToPrint.append(workingCopyToPrint(fileInDirectory, pathOfFile));
        }

        return contentToPrint.toString();
    }

    public void clearWorkingCopyFiles(Path repositoryPath) throws IOException { ///@@@@@@@@@@@@?????????????????????
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
        if (wc.getmFiles().isEmpty()) {
            throw new WorkingCopyIsEmptyException();
        }
        FileItemHandler.zip(wc, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
        return wc.getSha1Code();
    }

    public SortedSet<Delta.DeltaFileItem> getAllDeltaFilesFromCurrentWc() throws IOException {
        SortedSet<Delta.DeltaFileItem> deltaFiles = new TreeSet<>();
        WalkAction<Delta.DeltaFileItem> walkAction = new WalkAction<Delta.DeltaFileItem>() {
            @Override
            public void onWalkAction(FileItem file, Object... params) throws IOException {

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
                    //System.out.println("Dir:" + f.getAbsoluteFile());

                    Tree tree = new Tree(FileType.FOLDER, mUserName, mCommitDate, f.getName(), dirFiles);
                    //directoryFiles.add(tree);
                    wAction.onAddAction(set, directoryFiles, tree, f.getAbsolutePath());
                    wAction.onWalkAction(tree, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
                } else {
                    //System.out.println("File:" + f.getAbsoluteFile());
                    Blob blob = new Blob(f.getName(), FileHandler.readFile(f.getAbsolutePath()), FileType.FILE, mUserName, mCommitDate);
                    //directoryFiles.add(blob);
                    wAction.onAddAction(set, directoryFiles, blob, f.getAbsolutePath());
                    wAction.onWalkAction(blob, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
                }
            }
        }
    }
}