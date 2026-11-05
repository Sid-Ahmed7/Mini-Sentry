package com.sentry.backend.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

import com.sentry.backend.constants.CommonConstants;

import lombok.NoArgsConstructor;

@NoArgsConstructor 
public final class Secrets {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String randomHex(int bytes) {
        byte[] b = new byte[bytes];
        RANDOM.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }

    public static String sha256Hex(String s) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance(CommonConstants.SHA_256).digest(s.getBytes(CommonConstants.STANDARD_CHARSETS)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean constantTimeEquals(String firstConstant, String secondConstant) {
        if (firstConstant == null || secondConstant == null) {
            return false;
        }
        return MessageDigest.isEqual(firstConstant.getBytes(CommonConstants.STANDARD_CHARSETS), secondConstant.getBytes(CommonConstants.STANDARD_CHARSETS));
    }
}
