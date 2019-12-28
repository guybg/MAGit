package com.magit.logic.utils.file;

import com.magit.logic.system.objects.Blob;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.FileItem;
import com.magit.logic.utils.digest.Sha1;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileItemHandler {
    static void fileItemToFile(Blob fileItem, String destinationPath, String fileName) throws IOException {
        FileHandler.writeNewFile(Paths.get(destinationPath, fileName).toString(), fileItem.getFileContent());
    }

    static void fileItemToFile(String destinationPath, String fileName) {
        FileHandler.writeNewFolder(Paths.get(destinationPath, fileName).toString());
    }

    static void zip(FileItem fileItem, String destinationPath) throws IOException {
        FileHandler.writeNewFolder(Paths.get(destinationPath).toString());
        try (FileOutputStream fos = new FileOutputStream(Paths.get(destinationPath, fileItem.getSha1Code().toString()).toString());
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fos)) {
            gzipOutputStream.write(fileItem.getFileContent().getBytes());
            gzipOutputStream.flush();
        }
    }

    public static void zip(Commit commit, String destinationPath, Sha1 sha1) throws IOException {
        FileHandler.writeNewFolder(Paths.get(destinationPath).toString());
        try (FileOutputStream fos = new FileOutputStream(Paths.get(destinationPath, sha1.toString()).toString());
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fos)) {
            gzipOutputStream.write(commit.getFileContent().getBytes());
            gzipOutputStream.flush();
        }

    }

    public static String unzip(String sourcePath, Sha1 sourceSha1, String destinationPath, String fileName) throws IOException {
        try (FileInputStream fis = new FileInputStream(Paths.get(sourcePath, sourceSha1.toString()).toString());
             GZIPInputStream gzipInputStream = new GZIPInputStream(fis)) {
            new File(destinationPath).mkdirs();
            String fileContent = new String(IOUtils.toByteArray(gzipInputStream));
            FileHandler.writeNewFile(Paths.get(destinationPath, fileName).toString(), fileContent);
            return fileContent;
        }
    }

    public static String zipToString(String sourcePath, Sha1 sourceSha1) throws IOException {
        try (FileInputStream fis = new FileInputStream(Paths.get(sourcePath, sourceSha1.toString()).toString());
             GZIPInputStream gzipInputStream = new GZIPInputStream(fis)) {
            return new String(IOUtils.toByteArray(gzipInputStream));
        }
    }

    public static String zipToString(String sourcePath, String sourceSha1) throws IOException {
        return zipToString(sourcePath, new Sha1(sourceSha1,true));
    }
}
