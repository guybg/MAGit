package com.magit.logic.utils.file;

import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.FileItem;
import com.magit.logic.utils.digest.Sha1;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.zip.*;

public class FileZipper {
    private String mSourcePath;
    private String mDestinationPath;
    private String mZipName;
    private FileItem mFileItem;

    // public FileZipper(String mSourcePath, String mDestinationPath) {
    //     this.mSourcePath = mSourcePath;
    //     this.mDestinationPath = mDestinationPath;
    // }

    // public FileZipper(FileItem fileItem, String mDestinationPath) {
    //     this.mDestinationPath = mDestinationPath;
    // }
    public FileZipper() {
    }

    public static void zip(FileItem fileItem, String destinationPath) throws IOException {
        String s = Paths.get(destinationPath, fileItem.getSha1Code().toString()).toString();
        FileOutputStream fos = new FileOutputStream(Paths.get(destinationPath, fileItem.getSha1Code().toString()).toString());
        GZIPOutputStream gzos = new GZIPOutputStream(fos);
        String ss = fileItem.getFileContent();
        gzos.write(fileItem.getFileContent().getBytes());
        gzos.close();
    }

    public static void zip(Commit commit, String destinationPath, Sha1 sha1) throws IOException {
        FileOutputStream fos = new FileOutputStream(Paths.get(destinationPath, sha1.toString()).toString());
        GZIPOutputStream gzos = new GZIPOutputStream(fos);
        gzos.write(commit.toString().getBytes());
        gzos.close();
    }

    public static String unzip(String sourcePath, Sha1 sourceSha1, String destinationPath, String fileName) throws IOException {
        FileInputStream fis = new FileInputStream(Paths.get(sourcePath, sourceSha1.toString()).toString());

        GZIPInputStream gzis = new GZIPInputStream(fis);

        InputStreamReader reader = new InputStreamReader(gzis);
        String fileContent = new String(IOUtils.toByteArray(gzis));
        FileWriter.writeNewFile(Paths.get(destinationPath, fileName).toString(), fileContent);
        return fileContent;
    }

    public void zipFile(String zipName) throws IOException {
        mZipName = zipName;
        createZipFile(zipName);

        getZipTest();
    }

    private void createZipFile(String zipName) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(Paths.get(mDestinationPath, zipName).toString()));
             FileInputStream fileInputStream = new FileInputStream(mSourcePath)) {
            ZipEntry entry = new ZipEntry("file");
            zipOutputStream.putNextEntry(entry);
            IOUtils.copy(fileInputStream, zipOutputStream);
        }


    }

    private void getZipTest() throws IOException {
        ZipFile zipFile = new ZipFile(Paths.get(mDestinationPath, mZipName).toString());
        ZipEntry zipEntry = zipFile.getEntry("file");
        InputStream inputStream = zipFile.getInputStream(zipEntry);
        String s = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
}
