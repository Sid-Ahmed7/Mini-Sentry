package com.sentry.backend.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import lombok.NoArgsConstructor;

@NoArgsConstructor 
public final class CommonConstants {

public static final Charset STANDARD_CHARSETS = StandardCharsets.UTF_8;

/*Rabbit Config*/
    public static final String QUEUE = "events";
    public static final String DEAD_EXCHANGE = "events.dlx";
    public static final String DEAD_QUEUE = "events.dlq";
    public static final String DEAD_KEY = "events.dead";
    public static final long QUEUE_MAX_LENGTH = 100_000;
    public static final Integer QUEUE_TTL = 7 * 24 * 3600 * 1000;

/*Security */    
    /*IngestLimits */
    public static final Integer MAX_KEYS_IP = 50_000;
    public static final Integer MAX_KEYS_PROJECT = 10_000;
    
    /*Bucket - RateLimiter */
    public static final long NANOS_PER_SECOND = 1_000_000_000L;
    public static final long IDLE_NANOS = 120_000_000_000L;

    /*LoginAttempt */
    public static final Integer MAX_KEYS = 10_000;

    /*Secret */
    public static final String SHA_256 = "SHA-256";

/*Validation */
    public static final Integer MAX_FRAMES = 100;
    public static final Integer MAX_TAGS = 30;
    public static final Integer MAX_BREADCRUMBS = 50;
    public static final Pattern PLATFORM_PATTERN = Pattern.compile("^[a-z]{2,20}$");
    public static final Pattern RELEASE_PATTERN = Pattern.compile("^[A-Za-z0-9._+\\-]{1,64}$");
    public static final Pattern ENVIRONMENT_PATTERN = Pattern.compile("^[A-Za-z0-9._\\-]{1,32}$");

/*Payload Validator */
    public static final Integer MAX_REQUIRED = 200;
    public static final Integer MAX_OPTIONAL = 2000;
    public static final Integer MAX_OPTIONAL_URL = 500;
    public static final Integer MAX_OPTIONAL_FRAME_FUNCTION = 300;
    public static final Integer MAX_OPTIONAL_FRAME_FILE = 500;
    public static final Integer MAX_TAG_KEY = 64;
    public static final Integer MAX_BREADCRUMB_MESSAGE= 300;
    public static final Integer MAX_TAG_VALUE= 300;

/*Processing */
    /*SensitiveData */
    public static final String REDACTED = "[REDACTED]";
    public static final Pattern SENSITIVE_KEY = Pattern.compile("(?i)(pass(word|wd)?|secret|token|authorization|auth|cookie|session|api[-_]?key|credential|private)");
    public static final Pattern JWT = Pattern.compile("\\beyJ[A-Za-z0-9_-]{5,}\\.[A-Za-z0-9_-]{5,}\\.[A-Za-z0-9_-]{5,}\\b");
    public static final Pattern BEARER = Pattern.compile("(?i)\\b(bearer|basic)\\s+[A-Za-z0-9._~+/=-]{8,}");
    public static final Pattern KEY_VALUE = Pattern.compile("(?i)\\b(pass(?:word|wd)?|secret|token|api[-_]?key|authorization)\\s*[=:]\\s*[^\\s,;&\"']+");
    public static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    public static final Pattern LONG_HEX = Pattern.compile("\\b[A-Fa-f0-9]{32,}\\b");

    /*StackFrameClassifier */
    public static final String[] LIBRARY_MARKERS = {"node_modules", "/vendor", "vendor.", "webpack/runtime", "react-dom", "<anonymous>","java.", "javax.", "jakarta.", "jdk.", "sun.", "org.springframework", "org.apache","org.hibernate", "com.zaxxer", "com.sun.", "io.netty"};

    /*FingerPrintGenerator */
    public static final Integer MAX_APP_FRAMES = 5;
    public static final char LINE_SEPARATOR = '\n';
    public static final char FUNCTION_FILE_SEPARATOR = '|';

    /*MessageNormalizer */
    public static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    public static final Pattern HEX_PATTERN = Pattern.compile("\\b[0-9a-f]{8,}\\b");
    public static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    public static final String UUID_TOKEN = "<uuid>";
    public static final String HEX_TOKEN = "<hex>";
    public static final String NUMBER_TOKEN = "<n>";
    public static final Integer MAX_LENGTH = 200;

    /*Filepaths */
    public static final char QUERY_START = '?';
    public static final char FRAGMENT_START = '#';
    public static final char UNIX_SEPARATOR = '/';
    public static final char WINDOWS_SEPARATOR = '\\';

}
