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
    protected Sha1 mSha1Code;

    public FileItem(String mName,
                    FileType mFileType,
                    String mLastUpdater,
                    Date mLastModified,
                    Sha1 sha1Code) {
        this.mName = mName;
        this.mFileType = mFileType;
        this.mLastUpdater = mLastUpdater;
        this.mLastModified = mLastModified;
        this.mSha1Code = sha1Code;
    }

    public abstract String getFileContent();

    public String toPrintFormat(String path) {
        StringBuilder fileItemContent = new StringBuilder();

        fileItemContent.append(String.format(" --> %s%s", path, System.lineSeparator()));
        fileItemContent.append(String.format("%s%s", mSha1Code, System.lineSeparator()));
        fileItemContent.append(String.format("%s - %s%s", mName, mLastModified, System.lineSeparator()));
        fileItemContent.append(String.format("==============================================%s", System.lineSeparator()));

        return fileItemContent.toString();
    }

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

    public Sha1 getSha1Code() {
        if (mSha1Code == null)
            mSha1Code = new Sha1(getFileContent(), false);
        return mSha1Code;
    }

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
