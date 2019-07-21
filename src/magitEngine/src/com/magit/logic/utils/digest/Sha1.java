package com.magit.logic.utils.digest;

import org.apache.commons.codec.digest.DigestUtils;

public class Sha1 {
    private String mSha1Code;

    public Sha1(String input) {
        this.mSha1Code = DigestUtils.sha1Hex(input);
    }

}
