package com.magit.logic.system.objects;

import com.magit.logic.enums.FileType;
import com.magit.logic.utils.digest.Sha1;

import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

public abstract class FileItem implements Comparator<FileItem>, Comparable<FileItem> {
    private String mName;
    private FileType mFileType;
    protected String mLastUpdater;
    protected Date mLastModified;

    public FileItem(String mName,
                    FileType mFileType,
                    String mLastUpdater,
                    Date mLastModified) {
        this.mName = mName;
        this.mFileType = mFileType;
        this.mLastUpdater = mLastUpdater;
        this.mLastModified = mLastModified;
    }

    public abstract String getFileContent();

    public String getmLastUpdater() {
        return mLastUpdater;
    }

    public String getmName() {
        return mName;
    }

    public FileType getmFileType() {
        return mFileType;
    }

    public Date getLastModified() {
        return mLastModified;
    }

    public abstract Sha1 getSha1Code();

    public abstract String toString();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileItem)) return false;
        FileItem fileItem = (FileItem) o;
        return Objects.equals(mName, fileItem.mName) &&
                mFileType == fileItem.mFileType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mName, mFileType);
    }

    @Override
    public int compare(FileItem o1, FileItem o2) {
        return o1.mName.compareTo(o2.mName);
    }

    @Override
    public int compareTo(FileItem o) {
        return compare(this, o);
    }

}
