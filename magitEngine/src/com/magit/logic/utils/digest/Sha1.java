package com.magit.logic.utils.digest;

import org.apache.commons.codec.digest.DigestUtils;

public class Sha1 {
    private final String mSha1Code;

    public Sha1(String input, Boolean isSha1) {
        if (!isSha1)
            this.mSha1Code = DigestUtils.sha1Hex(input);
        else
            mSha1Code = input;
    }


    @Override
    public String toString() {
        return mSha1Code;
    }
}
