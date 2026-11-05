package com.sentry.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(AdminUser adminUser, IngestLimits ingestLimits) {

    public record AdminUser(String username, String password) {}
    public record IngestLimits(long maxBodyBytes, double projectRatePerSec, int projectBurst, double ipRatePerSec, int ipBurst) {}

}
