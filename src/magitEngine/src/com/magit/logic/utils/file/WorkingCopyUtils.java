package com.magit.logic.utils.file;

import com.magit.logic.enums.FileStatus;
import com.magit.logic.enums.FileType;
import com.magit.logic.exceptions.WorkingCopyIsEmptyException;
import com.magit.logic.system.interfaces.WalkAction;
import com.magit.logic.system.interfaces.WalkCompareAction;
import com.magit.logic.system.objects.Blob;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.FileItem;
import com.magit.logic.system.objects.Tree;
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
import java.util.stream.Collectors;

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

    public void clearWorkingCopyFiles(Path repositoryPath) throws IOException {
        FileHandler.clearFolder(repositoryPath);
    }


    public static Tree getWorkingCopyTreeFromCommit(Commit commit, String repositoryPath) throws IOException, ParseException {
        Sha1 wcSha1 = commit.getmWorkingCopySha1();

        return (Tree) walk(wcSha1, FileType.FOLDER, commit.getmLastUpdater(), commit.getLastModified(), commit.getmName(), repositoryPath);
    }

    private static FileItem walk(Sha1 sha1Code,
                                 FileType mFileType,
                                 String mLastUpdater,
                                 Date mCommitDate,
                                 String mName, String repositoryPath) throws IOException, ParseException {
        if (mFileType == FileType.FOLDER) {
            SortedSet<FileItem> files = new TreeSet<>();
            ArrayList<String[]> fileItems = Tree.treeItemsToStringArray(FileZipper.zipToString(Paths.get(repositoryPath, ".magit", "objects").toString(), sha1Code));
            for (String[] fileItem : fileItems) {
                DateFormat formatter1;
                formatter1 = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
                Date date = formatter1.parse(fileItem[4]);
                files.add(walk(new Sha1(fileItem[1], true), FileType.valueOf(fileItem[2]), fileItem[3], date, fileItem[0], repositoryPath));
            }
            return new Tree(mName, sha1Code, FileType.FOLDER, mLastUpdater, mCommitDate, files);

        } else {
            String fileContent = FileZipper.zipToString(Paths.get(repositoryPath, ".magit", "objects").toString(), sha1Code);
            return new Blob(mName, fileContent, mFileType, mLastUpdater, mCommitDate);
        }
    }


    public void unzipWorkingCopyFromCommit(Commit commit, String destinationPath) throws IOException, ParseException {
        Tree wc = getWorkingCopyTreeFromCommit(commit, mRepositoryDirectoryPath);
        WalkAction walkAction = (file, params) -> {
            if (file.getmFileType() == FileType.FILE)
                FileZipper.fileItemToFile((Blob) file, (String) params[0], ((String) params[1]));
            else
                FileZipper.fileItemToFile((Tree) file, (String) params[0], ((String) params[1]));
            return 1;
        };
        fileItemWalk(wc, destinationPath, walkAction);
    }

    public void zipWorkingCopyFromTreeWC(Tree wc) throws IOException, ParseException {
        WalkAction walkAction = (file, a) -> {
            FileZipper.zip(file, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
            return 1;
        };
        fileItemWalk(wc, mRepositoryDirectoryPath, walkAction);

    }

    private void fileItemWalk(FileItem fileItem, String destinationPath, WalkAction aAction) throws IOException {
        if (fileItem.getmFileType() == FileType.FILE || ((Tree) fileItem).getNumberOfFiles() == 0) {
            if (fileItem.getmFileType() == FileType.FILE) {
                aAction.action(fileItem, destinationPath, fileItem.getmName());
            }
            return;
        } else {

        }
        if (fileItem.getmName() != null) {
            aAction.action(fileItem, destinationPath, fileItem.getmName());
            destinationPath = Paths.get(destinationPath, fileItem.getmName()).toString();
        }
        for (FileItem file : ((Tree) fileItem).getmFiles()) {
            fileItemWalk(file, Paths.get(destinationPath).toString(), aAction);
        }

    }

    //T1 = WC
    //T2 = DELTA
    private static <T1, T2> T1 updateWalk(Tree newWc, Tree oldWc, T1 wc, WalkCompareAction<T1, T2> aAction, String currentPath) throws IOException {
        SortedSet<FileItem> tr1 = new TreeSet<>(CollectionUtils.select(newWc.getmFiles(), treePredicate));
        SortedSet<FileItem> bl1 = new TreeSet<>(CollectionUtils.select(newWc.getmFiles(), blobPredicate));
        SortedSet<FileItem> tr2 = new TreeSet<>(CollectionUtils.select(oldWc.getmFiles(), treePredicate));
        SortedSet<FileItem> bl2 = new TreeSet<>(CollectionUtils.select(oldWc.getmFiles(), blobPredicate));
        SortedSet<FileItem> dif = new TreeSet<>(CollectionUtils.union(CollectionUtils.intersection(bl2, bl1), bl1));
        //wc.setmFiles(dif);
        aAction.actionOnDelta(wc, aAction.delta(bl1, bl2, currentPath));

        if (tr1.isEmpty()) {
            if (!bl1.isEmpty()) {
                //return wc;
                return aAction.returnAction(wc);
            }
        }
        for (FileItem tree : tr1) {
            Boolean isNewFolder = true;
            for (FileItem tree2 : tr2) {
                if (tree2.getmName().equals(tree.getmName())) {
                    String updatater = tree.getmLastUpdater();
                    Date date = tree.getLastModified();
                    if (CollectionUtils.isEqualCollection(((Tree) tree).getmFiles(), ((Tree) tree2).getmFiles())) {
                        updatater = tree2.getmLastUpdater();
                        date = tree2.getLastModified();
                    }
                    //wc.addFileItem(updateWalk((Tree) tree, (Tree) tree2, new Tree(tree.getmFileType(), updatater, date, tree.getmName(), new TreeSet<FileItem>())));
                    aAction.action1(updateWalk((Tree) tree, (Tree) tree2, aAction.action2(tree.getmFileType(), updatater, date, tree.getmName(), new TreeSet<FileItem>(), wc), aAction, Paths.get(currentPath, tree.getmName()).toString()), wc);
                    isNewFolder = false;
                }
            }
            if (isNewFolder) {
                //wc.addFileItem(tree);
                aAction.action2(tree, wc);
                isNewFolder = true;
            }
        }
        return wc;
    }

    //file1 newfiles, file2 oldfiles (blobs)
    public static Map<FileStatus, ArrayList<String>> getWorkingCopyStatus(Tree newWc, Tree oldWc, String currentPath) throws IOException {
        WalkCompareAction<Map<FileStatus, ArrayList<String>>, Map<FileStatus, ArrayList<String>>> actionInterface = new WalkCompareAction<Map<FileStatus, ArrayList<String>>, Map<FileStatus, ArrayList<String>>>() {
            @Override
            public Map<FileStatus, ArrayList<String>> delta(SortedSet<FileItem> file1, SortedSet<FileItem> file2, String currentPath) throws IOException {
                Map<FileStatus, ArrayList<String>> files = new TreeMap<>();
                // new files
                ArrayList<String> newAndEditedFiles = new ArrayList<>(CollectionUtils.subtract(file1, file2).stream().map(FileItem::getmName).collect(Collectors.toList()));
                ArrayList<String> editedFiles = new ArrayList<>(newAndEditedFiles.stream()
                        .filter(a -> file2.stream()
                                .map(FileItem::getmName)
                                .anyMatch(name -> name.equals(a)))
                        .collect(Collectors.toList()));
                // Edited files
                ArrayList<String> newFiles = new ArrayList<>(CollectionUtils.subtract(newAndEditedFiles, editedFiles));
                //deleted Files
                ArrayList<String> deletedFiles = new ArrayList<>(CollectionUtils.subtract(file2.stream().map(FileItem::getmName).collect(Collectors.toList()), file1.stream().map(FileItem::getmName).collect(Collectors.toList())));

                //adding path
                ArrayList<String> editedFilesWithPath = new ArrayList<>(editedFiles.stream().map(a -> Paths.get(currentPath, a).toString()).collect(Collectors.toList()));
                ArrayList<String> newFilesWithPath = new ArrayList<>(newFiles.stream().map(a -> Paths.get(currentPath, a).toString()).collect(Collectors.toList()));
                ArrayList<String> deletedFilesWithPath = new ArrayList<>(deletedFiles.stream().map(a -> Paths.get(currentPath, a).toString()).collect(Collectors.toList()));

                files.put(FileStatus.EDITED, editedFilesWithPath);
                files.put(FileStatus.NEW, newFilesWithPath);
                files.put(FileStatus.REMOVED, deletedFilesWithPath);
                return files;
            }

            @Override
            public Map<FileStatus, ArrayList<String>> actionOnDelta(Map<FileStatus, ArrayList<String>> onWhat, Map<FileStatus, ArrayList<String>> delta) {
                onWhat.putAll(delta);
                return onWhat;
            }

            @Override
            public Map<FileStatus, ArrayList<String>> returnAction(Map<FileStatus, ArrayList<String>> obj) {
                return obj;
            }

            @Override
            public Map<FileStatus, ArrayList<String>> action1(Map<FileStatus, ArrayList<String>> obj, Map<FileStatus, ArrayList<String>> onWhat) {
                onWhat.putAll(obj);
                return null;
            }

            @Override
            public Map<FileStatus, ArrayList<String>> action2(Object... params) {
                ((Map<FileStatus, ArrayList<String>>) params[1]).putAll((Map<FileStatus, ArrayList<String>>) params[0]);
                return (Map<FileStatus, ArrayList<String>>) params[1];
            }
        };
        Map<FileStatus, ArrayList<String>> map = new TreeMap<>();
        map = updateWalk(newWc, oldWc, new TreeMap<>(), actionInterface, currentPath);
        return map;
    }

    public static Tree getWcWithOnlyNewchanges(Tree newWc, Tree oldWc) throws IOException {
        Tree wc;

        WalkCompareAction<Tree, SortedSet<FileItem>> actionInterface = new WalkCompareAction<Tree, SortedSet<FileItem>>() {
            @Override
            public SortedSet<FileItem> delta(SortedSet<FileItem> file1, SortedSet<FileItem> file2, String currentPath) {
                SortedSet<FileItem> dif = new TreeSet<>(CollectionUtils.union(CollectionUtils.intersection(file2, file1), file1));
                return dif;
            }

            @Override
            public Tree actionOnDelta(Tree onWhat, SortedSet<FileItem> delta) {
                onWhat.setmFiles(delta);
                return onWhat;
            }

            @Override
            public Tree returnAction(Tree obj) {
                return obj;
            }

            @Override
            public Tree action1(Tree obj, Tree onWhat) {
                onWhat.addFileItem(obj);
                return onWhat;
            }

            @Override
            public Tree action2(Object... params) {
                ((Tree) params[1]).addFileItem((Tree) params[0]);
                return ((Tree) params[1]);
            }
        };
        if (CollectionUtils.isEqualCollection(newWc.getmFiles(), oldWc.getmFiles()))
            wc = oldWc;
        else
            wc = updateWalk(newWc, oldWc, new Tree(FileType.FOLDER, newWc.getmLastUpdater(), newWc.getLastModified(), newWc.getmName(), new TreeSet<>()), actionInterface, null);
        return wc;
    }

    public Sha1 zipWorkingCopyFromCurrentWorkingCopy() throws IOException, WorkingCopyIsEmptyException {
        SortedSet<FileItem> directoryFiles = new TreeSet<>();
        WalkAction action = (file, params) -> {
            FileZipper.zip(file, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
            return 1;
        };
        wcWalk(mRepositoryDirectoryPath, directoryFiles, action);
        Tree wc = new Tree(FileType.FOLDER, mUserName, mCommitDate, "wc", directoryFiles);
        if (wc.getmFiles().isEmpty()) {
            throw new WorkingCopyIsEmptyException();
        }
        FileZipper.zip(wc, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
        return wc.getSha1Code();
    }

    public Tree getWc() throws IOException {
        SortedSet<FileItem> directoryFiles = new TreeSet<>();
        wcWalk(mRepositoryDirectoryPath, directoryFiles, (file, params) -> 1);
        Tree wc = new Tree(FileType.FOLDER, mUserName, mCommitDate, "wc", directoryFiles);
        return wc;
    }

    private void wcWalk(String repositoryDirectoryPath, SortedSet<FileItem> directoryFiles, WalkAction wAction) throws IOException {
        File root = new File(repositoryDirectoryPath);
        File[] list = root.listFiles();
        if (list == null) return;

        for (File f : list) {
            if (!f.getName().equals(".magit")) {
                if (f.isDirectory()) {
                    if (f.listFiles().length == 0) continue;
                    SortedSet<FileItem> dirFiles = new TreeSet<>();
                    wcWalk(f.getAbsolutePath(), dirFiles, wAction);
                    System.out.println("Dir:" + f.getAbsoluteFile());

                    Tree tree = new Tree(FileType.FOLDER, mUserName, mCommitDate, f.getName(), dirFiles);
                    directoryFiles.add(tree);
                    wAction.action(tree, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
                } else {
                    System.out.println("File:" + f.getAbsoluteFile());
                    Blob blob = new Blob(f.getName(), FileReader.readFile(f.getAbsolutePath()), FileType.FILE, mUserName, mCommitDate);
                    directoryFiles.add(blob);
                    wAction.action(blob, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
                }
            }
        }

    }

    public String getWorkingCopyContent(Tree workingCopy) {
        return toPrintFormat(workingCopy);
    }

    private String toPrintFormat(Tree workingCopyFolder) {
        StringBuilder workingCopyContent = new StringBuilder();

        workingCopyContent.append(String.format("Files Information ( . == [%s] )%s",
                mRepositoryDirectoryPath, System.lineSeparator()));
        workingCopyContent.append(String.format("==============================================%s", System.lineSeparator()));
        workingCopyContent.append(String.format("[Root Folder] --> .%s", System.lineSeparator()));
        workingCopyContent.append(String.format("%s%s", workingCopyFolder.getSha1Code(), System.lineSeparator()));
        workingCopyContent.append(String.format("%s - %s%s", workingCopyFolder.getmName(), workingCopyFolder.getLastModified(), System.lineSeparator()));
        workingCopyContent.append(String.format("==============================================%s", System.lineSeparator()));
        for (FileItem fileToPrint : workingCopyFolder.listFiles()) {
            workingCopyContent.append(workingCopyToPrint(fileToPrint, Paths.get(".")));
        }

        workingCopyContent.append(
                String.format("Current User: %s      Current Repository Location: %s %s",
                        mUserName, mRepositoryDirectoryPath, System.lineSeparator()));

        return workingCopyContent.toString();
    }

    private String workingCopyToPrint(FileItem fileToPrint, Path pathToFile) {
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
}