package com.magit.logic.system.objects;

import com.magit.logic.enums.FileStatus;

public class MergeStateFileItem {

    private FileItem ours;
    private FileItem theirs;
    private FileItem ancestor;

    private FileStatus oursTheirs;
    private FileStatus theirsAncestor;
    private FileStatus ancestorOurs;

    public MergeStateFileItem(FileItem ours, FileItem theirs, FileItem ancestor,
                              FileStatus oursTheirs, FileStatus theirsAncestor, FileStatus ancestorOurs) {

        this.ours = ours;
        this.theirs = theirs;
        this.ancestor = ancestor;
        this.oursTheirs = oursTheirs;
        this.theirsAncestor = theirsAncestor;
        this.ancestorOurs = ancestorOurs;
    }


    public int getStatus() {
        int output = 0;

        if (null != ours)
            output |= 0b100000;
        if (null != theirs)
            output |= 0b010000;
        if (null != ancestor)
            output |= 0b001000;

        if (oursTheirs == FileStatus.EDITED)
            output |= 0b000100;
        if (theirsAncestor == FileStatus.EDITED)
            output |= 0b000010;
        if (ancestorOurs == FileStatus.EDITED)
            output |= 0b000001;

        return output;
    }
}
