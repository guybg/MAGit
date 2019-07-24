package com.magit.logic.utils.file;

import com.magit.logic.enums.FileType;
import com.magit.logic.system.objects.Blob;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.FileItem;
import com.magit.logic.system.objects.Tree;
import com.magit.logic.utils.digest.Sha1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

public class WorkingCopyWalker {
    private String mRepositoryDirectoryPath;
    private String mUserName;
    private Date mCommitDate;

    public WorkingCopyWalker(String repositoryDirectoryPath, String userName, Date commitDate) {
        mRepositoryDirectoryPath = repositoryDirectoryPath;
        mUserName = userName;
        mCommitDate = commitDate;
    }

    public Sha1 zipWorkingCopy(String repositoryDirectoryPath) throws IOException {
        SortedSet<FileItem> directoryFiles = new TreeSet<>();
        zipWalk(repositoryDirectoryPath, directoryFiles);
        Tree wc = new Tree(FileType.FOLDER, "administrator", mCommitDate, "wc", directoryFiles);
        FileZipper.zip(wc, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
        return wc.getSha1Code();
    }

    public FileItem getWorkingCopyTreeFromCommit(Commit commit) throws IOException, ParseException {
        Sha1 wcSha1 = commit.getmWorkingCopySha1();
        String wcContent = FileZipper.zipToString(Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString(), wcSha1);
        ArrayList<String[]> fileItems = Tree.treeItemsToStringArray(FileZipper.zipToString(Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString(), commit.getmWorkingCopySha1()));
        SortedSet<FileItem> files = new TreeSet<>();
        DateFormat formatter1;
        formatter1 = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        return walk(wcSha1, FileType.FOLDER, commit.getmLastUpdater(), commit.getmCommitDate(), commit.getmName(), files);
    }

    private FileItem walk(Sha1 sha1Code,
                          FileType mFileType,
                          String mLastUpdater,
                          Date mCommitDate,
                          String mName,
                          SortedSet<FileItem> mFiles) throws IOException, ParseException {
        if (mFileType == FileType.FOLDER) {
            SortedSet<FileItem> files = new TreeSet<>();
            ArrayList<String[]> fileItems = Tree.treeItemsToStringArray(FileZipper.zipToString(Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString(), sha1Code));
            for (String[] fileItem : fileItems) {
                DateFormat formatter1;
                formatter1 = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
                Date date = formatter1.parse(fileItem[4]);
                files.add(walk(new Sha1(fileItem[1], true), FileType.valueOf(fileItem[2]), fileItem[3], date, fileItem[0], files));
            }
            return new Tree(mName, sha1Code, FileType.FOLDER, mLastUpdater, mCommitDate, files);

        } else {
            String fileContent = FileZipper.zipToString(Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString(), sha1Code);
            return new Blob(mName, fileContent, mFileType, mLastUpdater, mCommitDate);
        }
    }

    //(String sourcePath, Sha1 sourceSha1, String destinationPath, String fileName)
    public void unzipWorkingCopy(Commit commit, String destenationPath) {
        // Tree tree = getWorkingCopyTreeFromCommit(commit);

    }

    private void zipWalk(String repositoryDirectoryPath, SortedSet<FileItem> directoryFiles) throws IOException {
        File root = new File(repositoryDirectoryPath);
        File[] list = root.listFiles();
        if (list == null) return;

        for (File f : list) {
            if (!f.getName().equals(".magit")) {
                if (f.isDirectory()) { /// <------- problem with f.getName()
                    String check = f.getName();
                    SortedSet<FileItem> dirFiles = new TreeSet<>();
                    zipWalk(f.getAbsolutePath(), dirFiles);
                    System.out.println("Dir:" + f.getAbsoluteFile());

                    Tree tree = new Tree(FileType.FOLDER, mUserName, mCommitDate, f.getName(), dirFiles);
                    directoryFiles.add(tree);
                    FileZipper.zip(tree, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
                } else {
                    System.out.println("File:" + f.getAbsoluteFile());
                    Blob blob = new Blob(f.getName(), FileReader.readFile(f.getAbsolutePath()), FileType.FILE, mUserName, mCommitDate);
                    directoryFiles.add(blob);
                    FileZipper.zip(blob, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
                }
            }
        }
    }


}