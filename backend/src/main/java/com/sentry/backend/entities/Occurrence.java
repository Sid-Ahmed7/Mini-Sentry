package com.sentry.backend.entities;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity 
@Table(name = "occurrence")
@AllArgsConstructor  
@Getter 
@Setter 
public class Occurrence {

    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "issue_id", nullable = false)
    private UUID issueId;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "app_release")
    private String release;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    public Occurrence(UUID issueId, Instant receivedAt, String release, String payload) {
        this.issueId = issueId;
        this.receivedAt = receivedAt;
        this.release = release;
        this.payload = payload;
    }
}
