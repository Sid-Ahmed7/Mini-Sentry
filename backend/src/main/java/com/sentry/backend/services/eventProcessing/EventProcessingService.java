package com.sentry.backend.services.eventProcessing;


import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.sentry.backend.dto.request.EventRequest;
import com.sentry.backend.dto.request.FrameRequest;
import com.sentry.backend.entities.Issue;
import com.sentry.backend.entities.Occurrence;
import com.sentry.backend.enumeration.IssueStatus;
import com.sentry.backend.live.LiveStream;
import com.sentry.backend.messaging.EventMessage;
import com.sentry.backend.processing.FingerprintGenerator;
import com.sentry.backend.processing.StackFrameClassifier;
import com.sentry.backend.processing.message.FilePaths;
import com.sentry.backend.repository.IssueRepository;
import com.sentry.backend.repository.OccurrenceRepository;
import com.sentry.backend.repository.ProjectRepository;
import com.sentry.backend.services.sourceMap.SourceMapService;

import jakarta.transaction.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Service
public class EventProcessingService implements IEventProcessingService {

    private final ProjectRepository projects;
    private final IssueRepository issues;
    private final OccurrenceRepository occurrenceRepository;
    private final SourceMapService sourceMapService;
    private final FingerprintGenerator fingerprintGenerator;
    private final LiveStream liveStream;
    private final JsonMapper mapper;

    public EventProcessingService(ProjectRepository projects, IssueRepository issues, OccurrenceRepository occurrenceRepository, SourceMapService sourceMapService, FingerprintGenerator fingerprintGenerator, LiveStream liveStream, JsonMapper mapper) {
        this.projects = projects;
        this.issues = issues;
        this.occurrenceRepository = occurrenceRepository;
        this.sourceMapService = sourceMapService;
        this.fingerprintGenerator = fingerprintGenerator;
        this.liveStream = liveStream;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void process(EventMessage message) {

        if (!projects.existsById(message.projectId())) {
            return;
        }

        EventRequest event = sourceMapService.apply(message.projectId(), message.eventRequest());
        String fingerprint = fingerprintGenerator.generate(event);

        Optional<Issue> existing = issues.findForUpdate(message.projectId(), fingerprint);
        boolean isNew = existing.isEmpty();
        Issue issue = existing.orElseGet(() -> {
            Issue newIssue = new Issue();
            newIssue.setProjectId(message.projectId());
            newIssue.setFingerprints(fingerprint);
            newIssue.setType(event.type());
            newIssue.setTitle(title(event));
            newIssue.setSourceLocation(culprit(event));
            newIssue.setStatus(IssueStatus.OPEN);
            newIssue.setRegression(false);
            newIssue.setEventCount(0);
            newIssue.setFirstSeen(message.receivedAt());
            newIssue.setLastSeen(message.receivedAt());
            return newIssue;
        });

        boolean regressed = issue.recordIssue(message.receivedAt(), event.release());
        issues.save(issue);
        occurrenceRepository.save(new Occurrence(issue.getId(), message.receivedAt(), event.release(), toJson(event)));

        if (isNew || regressed) {
            log.info("ALERT {} issue={} project={}", isNew ? "new-issue" : "regression", issue.getId(), issue.getProjectId());
        }
        if (issue.getStatus() != IssueStatus.IGNORED) {
            afterCommit(() -> liveStream.publish(issue));
        }
    }

    private static String title(EventRequest event) {
        String message = event.message() == null || event.message().isBlank() ? "" : ": " + event.message();
        String title = event.type() + message;
        return title.length() > 500 ? title.substring(0, 500) : title;
    }

    private static String culprit(EventRequest event) {
        for (FrameRequest frame : event.allFrames()) {
            if (StackFrameClassifier.isApplicationCode(frame)) {
                String culprit = (frame.function() == null ? "?" : frame.function()) + " (" + FilePaths.baseName(frame.file()) + (frame.line() != null ? ":" + frame.line() : "") + ")";
                return culprit.length() > 300 ? culprit.substring(0, 300) : culprit;
            }
        }
        return null;
    }

    private String toJson(EventRequest event) {
        return mapper.writeValueAsString(event);
    }

    private static void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
