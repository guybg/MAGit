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


    //(String sourcePath, Sha1 sourceSha1, String destinationPath, String fileName)
    public void unzipWorkingCopy(Commit commit, String destenationPath) {
        Sha1 wc = commit.getmWorkingCopySha1();
        // unzipWalk(destenationPath, wc,FileType.FOLDER, "WC");

    }

    //  private void unzipWalk(String destinationPath, Sha1 sha1Item, FileType fileType, String fileName){
    //      File root = new File(destinationPath);
    //      File[] list = root.listFiles();
    //      if (list == null) return;
//
    //      for (File f : list) {
    //          if (!f.getName().equals(".magit")) {
    //              if (f.isDirectory()) { /// <------- problem with f.getName()
    //                  String check = f.getName();
    //                  SortedSet<FileItem> dirFiles = new TreeSet<>();
    //                  unzipWalk(f.getAbsolutePath());
    //                  System.out.println("Dir:" + f.getAbsoluteFile());
//
    //                  Tree tree = new Tree(FileType.FOLDER, mUserName, mCommitDate, f.getName(), dirFiles);
    //                  directoryFiles.add(tree);
    //                  FileZipper.zip(tree, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
    //              } else {
    //                  System.out.println("File:" + f.getAbsoluteFile());
    //                  Blob blob = new Blob(f.getName(), FileReader.readFile(f.getAbsolutePath()), FileType.FILE, mUserName, mCommitDate);
    //                  directoryFiles.add(blob);
    //                  FileZipper.zip(blob, Paths.get(mRepositoryDirectoryPath, ".magit", "objects").toString());
    //              }
    //          }
    //      }
    //  }
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