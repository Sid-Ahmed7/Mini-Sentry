package com.sentry.backend.processing.message;

import com.sentry.backend.common.CurrentLocale;
import com.sentry.backend.constants.CommonConstants;

import lombok.AllArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
@AllArgsConstructor
public class Hashing {

    private final MessageSource messageSource;


    public String sha256Hex(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance(CommonConstants.SHA_256);
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(messageSource.getMessage("hashing.sha256.unavailable", new Object[]{CommonConstants.SHA_256}, CurrentLocale.getCurrentLocale()), e);
        }
    }
}
