package com.magit.logic.utils.file;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FileHandler {

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

    public static String readFile(String filePath) throws IOException {
        File file = new File(filePath);
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }

    public static void writeNewFile(String destinationPath, String fileContent) throws IOException {
        File file = new File(destinationPath);
        FileUtils.writeStringToFile(file, fileContent, StandardCharsets.UTF_8);
    }

    public static void writeNewFolder(String destinationPath) {
        File file = new File(destinationPath);
        file.mkdirs();
    }

    public static void appendFileWithContentAndLine(String destinationPath, String fileContent) throws IOException {
        File file = new File(destinationPath);
        FileUtils.writeStringToFile(file, fileContent + System.lineSeparator(), StandardCharsets.UTF_8, true);
    }

    public static boolean isContentExistsInFile(String fileSourcePath, String contentToFind) throws IOException {
        File file = new File(fileSourcePath);
        String[] commitsSha1s = FileUtils.readFileToString(file, StandardCharsets.UTF_8).split(System.lineSeparator());
        return Arrays.asList(commitsSha1s).contains(contentToFind);
    }
}
