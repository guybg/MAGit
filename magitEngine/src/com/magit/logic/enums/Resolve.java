package com.magit.logic.enums;

import com.magit.logic.system.objects.MergeStateFileItem;

public enum  Resolve {
    NewFile, Deleted, Edited, Conflict;

    public static Resolve resolve(MergeStateFileItem item) throws Exception {
        int status = item.getStatus();
        switch (status) {
            case 0b001000: // OPCH - Deleted
            case 0b101001: //OPEN CHANGE - OURS ==ANCESTOR, THEIRS DELETED
            case 0b011010: // OPEN CHANGE - THEIRS == ANCESTOR, OURS DELETED
            case 0b111100: //CONFLICT - BOTH EDITED ??? ***************ask Aviad****************
                return Deleted;
            case 0b100000: // OPEN CHANGE - OURS NEW
            case 0b010000: // OPCH - NEW
                return NewFile;
            case 0b110000: //CONFLICT OURS THEIRS NEW + DIFFERENT
            case 0b101000: //CONFLICT - OURS EDITED, THEIRS DELETED
            case 0b011000: //CONFLICT - OURS DELETED, THEIRS EDITED
            case 0b110100: //OPCH - NEW BUT EQUAL
            case 0b111000: //CONFILCT - ALL EDITED
                return Conflict;
            case 0b111010: // OPEN CHANGE - OURS EDITED
            case 0b111001: //OPEN CHANGE - THEIRS EDITED
            case 0b111111: //OPEN CHANGE - ALL IDENTICAL ???
                return Edited;
            default:
                throw new Exception();
        }
    }
}
