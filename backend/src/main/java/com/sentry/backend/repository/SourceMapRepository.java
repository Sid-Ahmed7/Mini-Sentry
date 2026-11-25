package com.sentry.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sentry.backend.entities.SourceMap;

public interface SourceMapRepository extends JpaRepository<SourceMap, UUID> {

    Optional<SourceMap> findByProjectIdAndAppReleaseAndFile(UUID projectId, String appRelease, String file);

    long countByProjectId(UUID projectId);
    
}
