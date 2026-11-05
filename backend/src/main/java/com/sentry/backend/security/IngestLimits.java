package com.sentry.backend.security;

import com.sentry.backend.config.AppProperties;
import com.sentry.backend.constants.CommonConstants;
import com.sentry.backend.security.rateLimiter.RateLimiter;

import org.springframework.stereotype.Component;
@Component
public class IngestLimits {

    private final RateLimiter perIp;
    private final RateLimiter perProject;

    public IngestLimits(AppProperties properties) {
        
        this.perIp = new RateLimiter(properties.ingestLimits().ipRatePerSec(), properties.ingestLimits().ipBurst(), CommonConstants.MAX_KEYS_IP);
        this.perProject = new RateLimiter(properties.ingestLimits().projectRatePerSec(), properties.ingestLimits().projectBurst(), CommonConstants.MAX_KEYS_PROJECT);
    }

    public boolean allowIp(String ip) {
        return perIp.tryAcquire(ip);
    }

    public boolean allowProject(String projectId) {
        return perProject.tryAcquire(projectId);
    }
}
