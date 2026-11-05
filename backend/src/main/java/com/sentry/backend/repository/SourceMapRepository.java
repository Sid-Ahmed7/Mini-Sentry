package com.sentry.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sentry.backend.entities.SourceMap;

public interface SourceMapRepository extends JpaRepository<SourceMap, UUID> {
    
}
