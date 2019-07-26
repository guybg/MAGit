package com.magit.logic.system.objects;

import com.magit.logic.enums.FileType;
import com.magit.logic.utils.digest.Sha1;
import org.apache.commons.collections4.CollectionUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Tree extends FileItem {
    private String mName;
    private Sha1 mSha1Code;
    private SortedSet<FileItem> mFiles;

    public Tree(FileType mFileType,
                String mLastUpdater,
                Date mCommitDate,
                String mName,
                SortedSet<FileItem> mFiles) {
        super(mName, mFileType, mLastUpdater, mCommitDate);
        this.mName = mName;
        this.mFiles = mFiles;
        this.mSha1Code = new Sha1(getFileContent(), false);
    }

    public Tree(
            String mName,
            Sha1 sha1Code,
            FileType mFileType,
            String mLastUpdater,
            Date mCommitDate,
            SortedSet<FileItem> mFiles) {
        super(mName, mFileType, mLastUpdater, mCommitDate);
        this.mName = mName;
        this.mFiles = mFiles;
        this.mSha1Code = sha1Code;
    }


    public static ArrayList<String[]> treeItemsToStringArray(String treeItems) throws ParseException {
        ArrayList<String[]> files = new ArrayList<>();
        String[] filesToSplit = treeItems.split(System.lineSeparator());
        for (String file : filesToSplit) {
            files.add(file.split(";"));
        }
        return files;
    }

    public void addFileItem(FileItem fileItem) {
        mFiles.add(fileItem);
        this.mSha1Code = new Sha1(getFileContent(), false);
    }

    public void setmFiles(SortedSet<FileItem> mFiles) {
        this.mFiles = mFiles;
        this.mSha1Code = new Sha1(getFileContent(), false);
    }

    @Override
    public String getFileContent() {
        String content = "";
        for (FileItem item : mFiles) {
            content += item.toString();
            content += System.lineSeparator();
        }
        return content;
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        return super.getmName() + ";" +
                mSha1Code + ";" +
                super.getmFileType() + ";" +
                super.getmLastUpdater() + ";" +
                dateFormat.format(super.getLastModified());
    }

    @Override
    public Sha1 getSha1Code() {
        if (mSha1Code == null)
            mSha1Code = new Sha1(getFileContent(), false);
        return mSha1Code;
    }

    //  @Override
    //  public boolean equals(Object o) {
    //      for(FileItem file: mFiles){
    //          file.equals()
    //      }
    //      return super.equals(o);
    //  }
//


    // @Override
    // public int hashCode() {
    //     return Objects.hash(mName, mSha1Code, mFiles);
    // }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tree)) return false;
        if (!super.equals(o)) return false;
        Tree tree = (Tree) o;
        return Objects.equals(mName, tree.mName) &&
                CollectionUtils.isEqualCollection(mFiles, tree.mFiles);
    }

    public String toPrintFormat(String repositoryPath) {
        StringBuilder contentOfTree = new StringBuilder();
        contentOfTree.append(String.format("Files Information ( '.' == [%s]%s", repositoryPath, System.lineSeparator()));

        LinkedList<FileItem> queue = new LinkedList<FileItem>();
        queue.add(this);
        return "dsds";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mName);
    }

    public int getNumberOfFiles() {
        return (mFiles == null) ? 0 : mFiles.size();
    }

    public SortedSet<FileItem> getmFiles() {
        return mFiles;
    }

}
