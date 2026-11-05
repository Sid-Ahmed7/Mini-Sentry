package com.sentry.backend.entities;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
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
@Table(name = "project")
@Getter
@Setter
@AllArgsConstructor 
public class Project {

    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "public_key" ,nullable = false, unique = true)
    private String publicKey;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "origins", nullable = false)
    private String origins = "";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public List<String> getAllOrigins(){
        if(origins == null || origins.isBlank()) {
            return List.of();
        }
        return Arrays.stream(origins.split(",")).map(String::trim).filter(origin -> !origin.isEmpty()).toList();
    }
}
