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
@Table(name = "source_map")
@Getter
@Setter
@AllArgsConstructor 
public class SourceMap {

    
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "app_release", nullable = false)
    private String appRelease;

    @Column(nullable = false)
    private String file;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();


    public void replaceContent(String newContent) {
        this.content = newContent;
        this.createdAt = Instant.now();
    }
}
