package com.sentry.backend.security.rateLimiter;

import com.sentry.backend.constants.CommonConstants;

final class Bucket {

    private final double ratePerSec;
    private final int burst;
    private double tokens;
    private long lastNanos;

    Bucket(double ratePerSec, int burst, long nowNanos) {
        this.ratePerSec = ratePerSec;
        this.burst = burst;
        this.lastNanos = nowNanos;
    }


    synchronized boolean tryConsume(long nowNanos) {
      tokens = Math.min(burst, tokens + (nowNanos - lastNanos) / CommonConstants.NANOS_PER_SECOND * ratePerSec);
        lastNanos = nowNanos;
        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    synchronized long lastNanos() {
        return lastNanos;
    }
}
