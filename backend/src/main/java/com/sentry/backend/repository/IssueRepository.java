package com.sentry.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sentry.backend.entities.Issue;

public interface IssueRepository extends JpaRepository<Issue, UUID> {
    
}
