package com.sentry.backend.messaging;

import java.util.UUID;

public record IssueUpdate(
        UUID issueId,
        UUID projectId,
        String title,
        String status,
        long count,
        boolean newIssue,
        boolean regression) {
}
