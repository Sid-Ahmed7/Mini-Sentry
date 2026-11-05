package com.sentry.backend.security.rateLimiter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

import com.sentry.backend.constants.CommonConstants;

public final class RateLimiter {
    
    private final double ratePerSec;
    private final int burst;
    private final int maxKeys;
    private final LongSupplier clock;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(double ratePerSec, int burst, int maxKeys) {
        this(ratePerSec, burst, maxKeys, System::nanoTime);
    }

    public RateLimiter(double ratePerSec, int burst, int maxKeys, LongSupplier clock) {
        this.ratePerSec = ratePerSec;
        this.burst = burst;
        this.maxKeys = maxKeys;
        this.clock = clock;
    }

    public boolean tryAcquire(String key) {
        if (buckets.size() >= maxKeys && !buckets.containsKey(key)) {
            evict();
        }
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(ratePerSec, burst, clock.getAsLong()));
        return bucket.tryConsume(clock.getAsLong());
    }

    private void evict() {
        long now = clock.getAsLong();
        buckets.entrySet().removeIf(e -> now - e.getValue().lastNanos() > CommonConstants.IDLE_NANOS);
        if (buckets.size() >= maxKeys) {
            buckets.clear();
        }
    }
}
