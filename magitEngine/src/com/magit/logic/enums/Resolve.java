package com.magit.logic.enums;

import com.magit.logic.system.objects.MergeStateFileItem;

public enum Resolve {
    OursNewFile, TheirsNewFile, OursDeleted, TheirsDeleted, BothDeleted, OursEdited, TheirsEdited, Conflict, UnChanged;

    public static Resolve resolve(MergeStateFileItem item) throws Exception {
        int status = item.getStatus();
        switch (status) {
            case 0b001000: // OPCH - Deleted at both sides
                return BothDeleted;
            case 0b101001: //OPEN CHANGE - OURS ==ANCESTOR, THEIRS DELETED (delete ours)
                return OursDeleted;
            case 0b011010: // OPEN CHANGE - THEIRS == ANCESTOR, OURS DELETED (delete theirs)
                return TheirsDeleted;
            case 0b100000: // OPEN CHANGE - OURS NEW
                return OursNewFile;
            case 0b010000: // OPCH - NEW
                return TheirsNewFile;
            case 0b110000: //CONFLICT OURS THEIRS NEW + DIFFERENT
            case 0b101000: //CONFLICT - OURS EDITED, THEIRS DELETED
            case 0b011000: //CONFLICT - OURS DELETED, THEIRS EDITED
            case 0b110100: //OPCH - NEW BUT EQUAL
            case 0b111000: //CONFILCT - ALL EDITED
            case 0b111100: //CONFLICT - BOTH EDITED ??? ***************ask Aviad****************
                return Conflict;
            case 0b111010: // OPEN CHANGE - OURS EDITED
                return OursEdited;
            case 0b111001: //OPEN CHANGE - THEIRS EDITED
                return TheirsEdited;
            case 0b111111: //OPEN CHANGE - ALL IDENTICAL ???
                return UnChanged;
            default:
                throw new Exception();
        }
    }
}
