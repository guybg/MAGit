package com.magit.logic.system.objects;

import com.magit.logic.enums.FileType;
import com.magit.logic.system.XMLObjects.MagitSingleFolder;
import com.magit.logic.utils.digest.Sha1;
import org.apache.commons.collections4.CollectionUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Tree extends FileItem {
    private SortedSet<FileItem> mFiles;

    public Tree(FileType mFileType, String mLastUpdater, Date mCommitDate, String mName, SortedSet<FileItem> mFiles) {
        super(mName, mFileType, mLastUpdater, mCommitDate, null);
        this.mFiles = mFiles;
        this.mSha1Code = new Sha1(getMinimizedFileContentForSha1(), false);
    }

    public Tree(String mName, Sha1 sha1Code, FileType mFileType, String mLastUpdater, Date mCommitDate,
                SortedSet<FileItem> mFiles) {
        super(mName, mFileType, mLastUpdater, mCommitDate, sha1Code);
        this.mFiles = mFiles;
        this.mSha1Code = new Sha1(getMinimizedFileContentForSha1(), false);
    }

    public Tree(MagitSingleFolder magitFolder) throws ParseException {
        super(magitFolder);
        this.mFiles = new TreeSet<>();
    }

    public static ArrayList<String[]> treeItemsToStringArray(String treeItems) {
        ArrayList<String[]> files = new ArrayList<>();
        String[] filesToSplit = treeItems.split(System.lineSeparator());
        for (String file : filesToSplit) {
            files.add(file.split(";"));
        }
        return files;
    }

    public String getName() {
        return super.getName();
    }

    public void addFileItem(FileItem fileItem) {
        mFiles.add(fileItem);
        this.mSha1Code = new Sha1(getMinimizedFileContentForSha1(), false);
    }

    @Override
    public String getFileContent() {
        StringBuilder content = new StringBuilder();
        for (FileItem item : mFiles) {
            content.append(item.toString());
            content.append(System.lineSeparator());
        }
        return content.toString();
    }

    public SortedSet<FileItem> listFiles() {
        return mFiles;
    }

    public String getMinimizedFileContentForSha1() {
        StringBuilder content = new StringBuilder();
        for (FileItem item : mFiles) {
            content.append(item.getMinimizedFileDetails());
            content.append(System.lineSeparator());
        }
        return content.toString();
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat(this.mDateFormat);
        return super.getName() + ";" +
                mSha1Code + ";" +
                super.getFileType() + ";" +
                super.getLastUpdater() + ";" +
                dateFormat.format(super.getLastModified());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tree)) return false;
        if (!super.equals(o)) return false;
        Tree tree = (Tree) o;
        return Objects.equals(super.getName(), tree.getName()) &&
                CollectionUtils.isEqualCollection(mFiles, tree.mFiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), super.getName());
    }

    @Override
    public String toPrintFormat(String pathToFile) {
        StringBuilder folderContent = new StringBuilder();

        folderContent.append("[FOLDER]");
        folderContent.append(super.toPrintFormat(pathToFile));

        return folderContent.toString();
    }

    public SortedSet<FileItem> getFiles() {
        return mFiles;
    }

    public void setFiles(SortedSet<FileItem> mFiles) {
        this.mFiles = mFiles;
        this.mSha1Code = new Sha1(getMinimizedFileContentForSha1(), false);
    }

    public int getNumberOfFiles() {
        return (mFiles == null) ? 0 : mFiles.size();
    }

}
