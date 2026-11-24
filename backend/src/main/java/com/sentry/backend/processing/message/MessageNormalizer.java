package com.sentry.backend.processing.message;

import java.util.Locale;

import com.sentry.backend.constants.CommonConstants;

import lombok.NoArgsConstructor;

@NoArgsConstructor 
public final class MessageNormalizer {

    public static String normalize(String message) {
        if (message == null) {
            return "";
        }
        String normalized = message.toLowerCase(Locale.ROOT);
    
        normalized = CommonConstants.UUID_PATTERN.matcher(normalized).replaceAll(CommonConstants.UUID_TOKEN);
        normalized = CommonConstants.HEX_PATTERN.matcher(normalized).replaceAll(CommonConstants.HEX_TOKEN);
        normalized = CommonConstants.NUMBER_PATTERN.matcher(normalized).replaceAll(CommonConstants.NUMBER_TOKEN);
        normalized = normalized.trim();
        return normalized.length() > CommonConstants.MAX_LENGTH ? normalized.substring(0, CommonConstants.MAX_LENGTH) : normalized;
    }
}