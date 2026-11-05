package com.sentry.backend.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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

}
