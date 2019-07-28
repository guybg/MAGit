package com.magit.logic.utils.file;

import com.magit.logic.system.objects.Blob;
import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.FileItem;
import com.magit.logic.system.objects.Tree;
import com.magit.logic.utils.digest.Sha1;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.zip.*;

public class FileItemHandler {


    public FileItemHandler() {
    }

    static void fileItemToFile(Blob fileItem, String destinationPath, String fileName) throws IOException {
        FileHandler.writeNewFile(Paths.get(destinationPath, fileName).toString(), fileItem.getFileContent());
    }

    static void fileItemToFile(Tree fileItem, String destinationPath, String fileName) {
        FileHandler.writeNewFolder(Paths.get(destinationPath, fileName).toString());
    }

    static void zip(FileItem fileItem, String destinationPath) throws IOException {
        FileOutputStream fos = new FileOutputStream(Paths.get(destinationPath, fileItem.getSha1Code().toString()).toString());
        GZIPOutputStream gzos = new GZIPOutputStream(fos);
        gzos.write(fileItem.getFileContent().getBytes());
        gzos.close();
    }

    public static void zip(Commit commit, String destinationPath, Sha1 sha1) throws IOException {
        FileOutputStream fos = new FileOutputStream(Paths.get(destinationPath, sha1.toString()).toString());
        GZIPOutputStream gzos = new GZIPOutputStream(fos);
        gzos.write(commit.getFileContent().getBytes());
        gzos.close();
    }

    public static String unzip(String sourcePath, Sha1 sourceSha1, String destinationPath, String fileName) throws IOException {
        FileInputStream fis = new FileInputStream(Paths.get(sourcePath, sourceSha1.toString()).toString());
        GZIPInputStream gzis = new GZIPInputStream(fis);
        String fileContent = new String(IOUtils.toByteArray(gzis));
        FileHandler.writeNewFile(Paths.get(destinationPath, fileName).toString(), fileContent);
        return fileContent;
    }

    public static String zipToString(String sourcePath, Sha1 sourceSha1) throws IOException {
        FileInputStream fis = new FileInputStream(Paths.get(sourcePath, sourceSha1.toString()).toString());
        GZIPInputStream gzis = new GZIPInputStream(fis);
        InputStreamReader reader = new InputStreamReader(gzis);
        return new String(IOUtils.toByteArray(gzis));
    }
}
