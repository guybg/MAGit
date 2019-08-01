package com.magit.logic.system.objects;

import com.magit.logic.enums.FileType;
import com.magit.logic.system.XMLObjects.MagitBlob;
import com.magit.logic.utils.digest.Sha1;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Blob extends FileItem {
    private String mFileContent;

    public Blob(String mName, String mFileContent, FileType mFileType, String mLastUpdater, Date mCommitDate) {
        super(mName, mFileType, mLastUpdater, mCommitDate, new Sha1(mFileContent, false));
        this.mFileContent = mFileContent;
    }

    public Blob(MagitBlob magitBlob) throws ParseException {
        super(magitBlob);
        this.mFileContent = magitBlob.getContent();
        this.mSha1Code = new Sha1(mFileContent, false);
    }

    @Override
    public String getFileContent() {
        return mFileContent;
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
        return super.getmName() + ";" +
                mSha1Code + ";" +
                super.getmFileType() + ";" +
                super.getmLastUpdater() + ";" +
                dateFormat.format(super.getLastModified());
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


    @Override
    public String toPrintFormat(String pathToBlob) {
        StringBuilder blobContent = new StringBuilder();
        blobContent.append("[BLOB]");
        blobContent.append(super.toPrintFormat(pathToBlob));

        return blobContent.toString();
    }
}
