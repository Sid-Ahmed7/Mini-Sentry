package com.sentry.backend.services;


import org.springframework.stereotype.Service;
import com.sentry.backend.repository.IssueRepository;
import com.sentry.backend.repository.OccurrenceRepository;
import com.sentry.backend.repository.ProjectRepository;

import tools.jackson.databind.json.JsonMapper;

@Service 
public class EventProcessingService implements IEventProcessingService {
    
    private final ProjectRepository projects;
    private final IssueRepository issues;
    private final OccurrenceRepository occurrenceRepository;
    private final JsonMapper mapper;

    public EventProcessingService(ProjectRepository projects, IssueRepository issues, OccurrenceRepository occurrenceRepository,JsonMapper mapper) {
        this.projects = projects;
        this.issues = issues;
        this.occurrenceRepository = occurrenceRepository;
        this.mapper = mapper;
    }


}
