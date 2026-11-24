package com.sentry.backend.processing;

import java.util.regex.Matcher;

import com.sentry.backend.constants.CommonConstants;

import lombok.NoArgsConstructor;

@NoArgsConstructor 
public class SensitiveData {
    

    public static String scrubText(String value) {
        if (value == null) {
            return null;
        }
        String res = CommonConstants.JWT.matcher(value).replaceAll(CommonConstants.REDACTED);
        res = CommonConstants.BEARER.matcher(res).replaceAll(r -> r.group(1) + " " + CommonConstants.REDACTED);
        res = CommonConstants.KEY_VALUE.matcher(res).replaceAll(r -> quote(r.group(1)) + "=" + CommonConstants.REDACTED);
        res = CommonConstants.EMAIL.matcher(res).replaceAll("[email]");
        res = CommonConstants.LONG_HEX.matcher(res).replaceAll(CommonConstants.REDACTED);
        return res;
    }

    public static String stripQuery(String url) {
        if (url == null) {
            return null;
        }
        int cut = url.length();
        int q = url.indexOf('?');
        int h = url.indexOf('#');
        if (q >= 0) {
            cut = Math.min(cut, q);
        }
        if (h >= 0) {
            cut = Math.min(cut, h);
        }
        return url.substring(0, cut);
    }

    private static String quote(String s) {
        return Matcher.quoteReplacement(s);
    }
}
