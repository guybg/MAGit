package com.magit.logic.system.objects;

public class FileItemInfo {
    private String fileName;
    private String fileType;
    private String fileSha1;
    private String fileLastUpdater;
    private String fileLastModified;
    private String fileContent;
    private String fileLocation;

    public FileItemInfo(String fileName, String fileType, String fileSha1, String fileLastUpdater, String fileLastModified, String fileContent,String fileLocation) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSha1 = fileSha1;
        this.fileLastUpdater = fileLastUpdater;
        this.fileLastModified = fileLastModified;
        this.fileContent = fileContent;
        this.fileLocation = fileLocation;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFileSha1() {
        return fileSha1;
    }

    public String getFileLastUpdater() {
        return fileLastUpdater;
    }

    public String getFileLastModified() {
        return fileLastModified;
    }

    public String getFileContent() {
        return fileContent;
    }

    public String getFileLocation() {
        return fileLocation;
    }
}
