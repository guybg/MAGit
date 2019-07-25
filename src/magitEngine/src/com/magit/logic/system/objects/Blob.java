package com.magit.logic.system.objects;

import com.magit.logic.enums.FileType;
import com.magit.logic.utils.digest.Sha1;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Blob extends FileItem {
    private String mFileContent;
    private Sha1 mSha1Code;

    public Blob(String mName,
                String mFileContent,
                FileType mFileType,
                String mLastUpdater,
                Date mCommitDate) {
        super(mName, mFileType, mLastUpdater, mCommitDate);
        this.mFileContent = mFileContent;
        this.mSha1Code = new Sha1(mFileContent, false);
    }

    @Override
    public String getFileContent() {
        return mFileContent;
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy-hh:mm:ss:sss");
        return super.getmName() + ";" +
                mSha1Code + ";" +
                super.getmFileType() + ";" +
                super.getmLastUpdater() + ";" +
                dateFormat.format(super.getmCommitDate());
    }

    @Override
    public Sha1 getSha1Code() {
        if (mSha1Code == null)
            mSha1Code = new Sha1(getFileContent(), false);
        return mSha1Code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mFileContent);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Blob)) return false;
        if (!super.equals(o)) return false;
        Blob blob = (Blob) o;
        return super.equals(o) && Objects.equals(mFileContent, blob.mFileContent);
    }

}
