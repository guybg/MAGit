package com.magit.logic.system.objects;

import com.magit.logic.enums.FileStatus;

public class MergeStateFileItem {
    private String path;
    private FileItem ours;
    private FileItem theirs;
    private FileItem ancestor;

    private FileStatus oursTheirs;
    private FileStatus theirsAncestor;
    private FileStatus oursAncestor;

    public MergeStateFileItem(FileItem ours, FileItem theirs, FileItem ancestor,
                              FileStatus oursTheirs, FileStatus theirsAncestor, FileStatus oursAncestor, String path) {
        this.path = path;
        this.ours = ours;
        this.theirs = theirs;
        this.ancestor = ancestor;
        this.oursTheirs = oursTheirs;
        this.theirsAncestor = theirsAncestor;
        this.oursAncestor = oursAncestor;
    }

    public int getStatus() {
        int output = 0;

        if (null != ours)
            output |= 0b100000;
        if (null != theirs)
            output |= 0b010000;
        if (null != ancestor)
            output |= 0b001000;

        if (oursTheirs == FileStatus.UNCHANGED && ours !=null && theirs!=null)
            output |= 0b000100;
        if (theirsAncestor == FileStatus.UNCHANGED && theirs !=null && ancestor!=null)
            output |= 0b000010;
        if (oursAncestor == FileStatus.UNCHANGED && ours !=null && ancestor!=null)
            output |= 0b000001;

        return output;
    }

    public String getPath() {
        return path;
    }

    public FileItem getOurs() {
        return ours;
    }

    public FileItem getTheirs() {
        return theirs;
    }

    public FileItem getAncestor() {
        return ancestor;
    }

    @Override
    public String toString() {
        String oursFileContent = "none";
        String theirsFileContent = "none";
        String ancestorFileContent = "none";
        if(ours != null){
            oursFileContent = ours.toString();
        }
        if(theirs != null){
            theirsFileContent = theirs.toString();
        }
        if(ancestor != null){
            ancestorFileContent = ancestor.toString();
        }
        return String.format("ours==%s=/=theirs==%s=/=ancestor==%s", oursFileContent,theirsFileContent,ancestorFileContent);
    }
}
