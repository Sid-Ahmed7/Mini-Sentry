package com.sentry.backend.security;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

import com.sentry.backend.constants.CommonConstants;

public class LoginAttempts {

    private final int maxFailures;
    private final long windowMillis;
    private final LongSupplier clock;
    private final ConcurrentHashMap<String, Deque<Long>> failures = new ConcurrentHashMap<>();

    public LoginAttempts(int maxFailures, long windowMillis) {
        this(maxFailures, windowMillis, System::currentTimeMillis);
    }

    public LoginAttempts(int maxFailures, long windowMillis, LongSupplier clock) {
        this.maxFailures = maxFailures;
        this.windowMillis = windowMillis;
        this.clock = clock;
    }

    public boolean isLocked(String key) {
        Deque<Long> deque = failures.get(key);
        if (deque == null) {
            return false;
        }
        synchronized (deque) {
            prune(deque);
            return deque.size() >= maxFailures;
        }
    }

    public void recordFailure(String key) {
        if (failures.size() >= CommonConstants.MAX_KEYS && !failures.containsKey(key)) {
            failures.clear();
        }
        Deque<Long> deque = failures.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (deque) {
            prune(deque);
            deque.addLast(clock.getAsLong());
        }
    }

    public void reset(String key) {
        failures.remove(key);
    }

    private void prune(Deque<Long> deque) {
        long limit = clock.getAsLong() - windowMillis;
        while (!deque.isEmpty() && deque.peekFirst() < limit) {
            deque.removeFirst();
        }
    }
}
