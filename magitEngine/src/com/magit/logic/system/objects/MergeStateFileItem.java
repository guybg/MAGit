package com.magit.logic.system.objects;

import com.magit.logic.enums.FileStatus;

public class MergeStateFileItem {

    private FileItem ours;
    private FileItem theirs;
    private FileItem ancestor;

    private FileStatus oursTheirs;
    private FileStatus theirsAncestor;
    private FileStatus oursAncestor;

    public MergeStateFileItem(FileItem ours, FileItem theirs, FileItem ancestor,
                              FileStatus oursTheirs, FileStatus theirsAncestor, FileStatus oursAncestor) {

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

        if (oursTheirs == FileStatus.UNCHANGED)
            output |= 0b000100;
        if (theirsAncestor == FileStatus.UNCHANGED)
            output |= 0b000010;
        if (oursAncestor == FileStatus.UNCHANGED)
            output |= 0b000001;

        return output;
    }
}
