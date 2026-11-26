package com.sentry.backend.dto.response;

import java.util.UUID;
import java.time.Instant;

public record IssueResponse(UUID id, UUID projectId, String type, String title, String culprit, String status, long eventCount, Instant firstSeen,Instant lastSeen, String lastRelease,boolean regression) {}