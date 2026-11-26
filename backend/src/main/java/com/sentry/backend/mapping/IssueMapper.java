package com.sentry.backend.mapping;

import com.sentry.backend.dto.response.IssueResponse;
import com.sentry.backend.entities.Issue;
import com.sentry.backend.messaging.IssueUpdate;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class IssueMapper {

    public static IssueResponse toResponse(Issue issue) {
        return new IssueResponse(
                issue.getId(),
                issue.getProjectId(),
                issue.getType(),
                issue.getTitle(),
                issue.getSourceLocation(),
                issue.getStatus().name(),
                issue.getEventCount(),
                issue.getFirstSeen(),
                issue.getLastSeen(),
                issue.getLastRelease(),
                issue.isRegression());
    }

    public static IssueUpdate toUpdate(Issue issue, boolean newIssue, boolean regression) {
        return new IssueUpdate(
                issue.getId(),
                issue.getProjectId(),
                issue.getTitle(),
                issue.getStatus().name(),
                issue.getEventCount(),
                newIssue,
                regression);
    }
}