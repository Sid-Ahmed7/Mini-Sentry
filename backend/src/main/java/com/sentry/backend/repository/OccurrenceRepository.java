package com.sentry.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sentry.backend.entities.Occurrence;

public interface OccurrenceRepository extends JpaRepository<Occurrence, UUID> {
    
}
