package com.magit.logic.utils.file;

import com.magit.logic.system.objects.Commit;
import com.magit.logic.system.objects.FileItem;
import com.magit.logic.utils.digest.Sha1;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileHandler {
    private String mSourcePath;
    private String mDestinationPath;
    private String mZipName;
    private FileItem mFileItem;

    public static void clearFolder(Path repositoryPath) throws IOException {
        File directory = new File(repositoryPath.toString());
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File fileToDelete : files) {
            if (!fileToDelete.getName().equals(".magit")) {

                if (fileToDelete.isDirectory())
                    FileUtils.deleteDirectory(fileToDelete);
                else
                    FileUtils.deleteQuietly(fileToDelete);
            }

        }
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
        writeNewFile(Paths.get(destinationPath, fileName).toString(), fileContent);
        return fileContent;
    }

    public static String zipToString(String sourcePath, Sha1 sourceSha1) throws IOException {
        FileInputStream fis = new FileInputStream(Paths.get(sourcePath, sourceSha1.toString()).toString());
        GZIPInputStream gzis = new GZIPInputStream(fis);
        InputStreamReader reader = new InputStreamReader(gzis);
        return new String(IOUtils.toByteArray(gzis));
    }

    private void createZipFile(String zipName) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(Paths.get(mDestinationPath, zipName).toString()));
             FileInputStream fileInputStream = new FileInputStream(mSourcePath)) {
            ZipEntry entry = new ZipEntry("file");
            zipOutputStream.putNextEntry(entry);
            IOUtils.copy(fileInputStream, zipOutputStream);
        }
    }

    public static String readFile(String filePath) throws IOException {
        File file = new File(filePath);
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }

    public static void writeNewFile(String destinationPath, String fileContent) throws IOException {
        File file = new File(destinationPath);
        FileUtils.writeStringToFile(file, fileContent, StandardCharsets.UTF_8);
    }

    static void writeNewFolder(String destinationPath) {
        File file = new File(destinationPath);
        file.mkdir();
    }
}
