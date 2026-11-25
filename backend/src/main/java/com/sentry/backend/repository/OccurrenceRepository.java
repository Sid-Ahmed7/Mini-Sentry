package com.sentry.backend.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sentry.backend.entities.Occurrence;

public interface OccurrenceRepository extends JpaRepository<Occurrence, UUID> {
    List<Occurrence> findTop20ByIssueIdOrderByReceivedAtDesc(UUID issueId);

    @Modifying
    @Query("DELETE FROM ErrorEvent e WHERE e.receivedAt < :cutoff")
    Integer deleteOlderThan(@Param("cutoff") Instant cutoff);

    @Query(value = "SELECT date_trunc('hour', received_at) as bucket, count(*) as total FROM error_event WHERE issue_id = :issueId AND received_at > now() - interval '24 hours' GROUP BY 1 ORDER BY 1", nativeQuery = true)
    List<Object[]> hourlyCounts(@Param("issueId") UUID issueId);
}
