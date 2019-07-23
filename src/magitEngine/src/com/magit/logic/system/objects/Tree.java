package com.magit.logic.system.objects;

import com.magit.logic.enums.FileType;
import com.magit.logic.utils.digest.Sha1;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.SortedSet;

public class Tree extends FileItem {
    private String mName;
    private String mTreeContent;
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
        this.mSha1Code = new Sha1(getFileContent());
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        return super.getmName() + ";" +
                mSha1Code + ";" +
                super.getmFileType() + ";" +
                dateFormat.format(super.getmCommitDate());
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
    public Sha1 getSha1Code() {
        return mSha1Code;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && this.getSha1Code().equals(((FileItem) o).getSha1Code());
    }

    @Override
    public int hashCode() {
        return Objects.hash(mName, mTreeContent, mSha1Code, mFiles);
    }

}
