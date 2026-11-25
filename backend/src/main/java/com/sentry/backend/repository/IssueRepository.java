package com.sentry.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sentry.backend.entities.Issue;
import com.sentry.backend.enumeration.IssueStatus;

import jakarta.persistence.LockModeType;

public interface IssueRepository extends JpaRepository<Issue, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Issue i WHERE i.projectId = :projectId AND i.fingerprint = :fingerprint")
    Optional<Issue> findForUpdate(@Param("projectId") UUID projectId, @Param("fingerprint") String fingerprint);

    Page<Issue> findByProjectIdAndStatus(UUID projectId, IssueStatus status, Pageable pageable);

    Page<Issue> findByProjectId(UUID projectId, Pageable pageable);

    Page<Issue> findByProjectIdAndRegressionTrueAndStatus(UUID projectId, IssueStatus status, Pageable pageable);
    
}
