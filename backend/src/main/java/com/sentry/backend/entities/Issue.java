package com.sentry.backend.entities;

import java.time.Instant;
import java.util.UUID;

import com.sentry.backend.enumeration.IssueStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity 
@Table(name = "issue")
@NoArgsConstructor 
@AllArgsConstructor 
@Getter 
@Setter 
public class Issue {

    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String fingerprints;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    private String sourceLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status = IssueStatus.OPEN;

    @Column(nullable = false)
    private boolean regression;

    @Column(name = "event_count", nullable = false)
    private long eventCount;

    @Column(name = "first_seen", nullable = false)
    private Instant firstSeen;

    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen;

    @Column(name = "last_release")
    private String lastRelease;

    @Column(name = "resolved_in_release")
    private String resolvedInRelease;

    public boolean recordIssue(Instant seenAt, String release) {
        boolean regressed = false;

        if(status == IssueStatus.RESOLVED && (release == null || !release.equals(resolvedInRelease))) {
            status = IssueStatus.OPEN;
            regression = true;
            regressed = true;
        }
        eventCount++;
        if(seenAt.isAfter(lastSeen)) {
            lastSeen = seenAt;
        }
        if (release != null) {
            lastRelease = release;
        }
    
        return regressed;
    }

    public void changeStatusIssue(IssueStatus status) {
        this.status = status;
        if(status == IssueStatus.RESOLVED) {
            this.resolvedInRelease = lastRelease;
            this.regression = false;
        } else if (status == IssueStatus.OPEN) {
            this.resolvedInRelease = null;
        }
    }
}
