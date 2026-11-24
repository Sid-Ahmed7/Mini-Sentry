package com.sentry.backend.processing.message;

import com.sentry.backend.constants.CommonConstants;

import lombok.NoArgsConstructor;

@NoArgsConstructor 
public class FilePaths {

     public static String baseName(String path) {
        if (path == null) {
            return "";
        }
        String cleaned = cutAt(path, CommonConstants.QUERY_START);
        cleaned = cutAt(cleaned, CommonConstants.FRAGMENT_START);
        int lastSeparator = Math.max(cleaned.lastIndexOf(CommonConstants.UNIX_SEPARATOR), cleaned.lastIndexOf(CommonConstants.WINDOWS_SEPARATOR));
        return lastSeparator >= 0 ? cleaned.substring(lastSeparator + 1) : cleaned;
    }

    private static String cutAt(String text, char marker) {
        int index = text.indexOf(marker);
        return index >= 0 ? text.substring(0, index) : text;
    }
    
}
