package com.sentry.backend.controller;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.context.MessageSource;

import com.sentry.backend.common.CurrentLocale;
import com.sentry.backend.config.AppProperties;
import com.sentry.backend.constants.CommonConstants;
import com.sentry.backend.dto.request.EventRequest;
import com.sentry.backend.entities.Project;
import com.sentry.backend.exceptions.InvalidPayloadException;
import com.sentry.backend.messaging.EventMessage;
import com.sentry.backend.processing.eventPaylod.EventPayloadRedactor;
import com.sentry.backend.repository.ProjectRepository;
import com.sentry.backend.security.IngestLimits;
import com.sentry.backend.security.Secrets;
import com.sentry.backend.validation.PayloadValidator;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Slf4j 
@RestController
@RequestMapping("/api/ingest")
public class EventCollectorController {

    private final MessageSource messageSource;
    private final ProjectRepository projectRepository;
    private final PayloadValidator payloadValidator;
    private final IngestLimits ingestLimits;
    private final RabbitTemplate rabbitTemplate;
    private final JsonMapper mapper;
    private final long maxBodyBytes;

    public EventCollectorController(MessageSource messageSource, ProjectRepository projectRepository, PayloadValidator payloadValidator, IngestLimits ingestLimits, RabbitTemplate rabbitTemplate, JsonMapper mapper, AppProperties properties) {
        this.messageSource = messageSource;
        this.projectRepository = projectRepository;
        this.payloadValidator = payloadValidator;
        this.ingestLimits = ingestLimits;
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
        this.maxBodyBytes = properties.ingestLimits().maxBodyBytes();
    }

    @PostMapping("/{projectId}/events")
    public ResponseEntity<Map<String, String>> ingest(@PathVariable UUID projectId, @RequestHeader(value = "X-MiniSentry-Key", required = false) String key, @RequestHeader(value = "Origin", required = false) String origin, HttpServletRequest request) throws IOException {

        if (!ingestLimits.allowIp(request.getRemoteAddr())) {
            return tooMany();
        }

        Project project = projectRepository.findById(projectId).orElse(null);
        
        if (project == null || !Secrets.constantTimeEquals(project.getPublicKey(), key)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", messageSource.getMessage("ingest.unauthorized", null, CurrentLocale.getCurrentLocale())));
        }

        if (!ingestLimits.allowProject(projectId.toString())) {
            return tooMany();
        }

        List<String> projectAllowed = project.getAllOrigins();

        if (!projectAllowed.isEmpty() && origin != null && !projectAllowed.contains(origin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", messageSource.getMessage("ingest.originNotAllowed", null, CurrentLocale.getCurrentLocale())));
        }

        if (request.getContentLengthLong() > maxBodyBytes) {
            return tooLarge();
        }

        byte[] body = request.getInputStream().readNBytes((int) maxBodyBytes + 1);
        if (body.length > maxBodyBytes) {
            return tooLarge();
        }

        EventRequest eventRequest;

        try {
            eventRequest = mapper.readValue(body, EventRequest.class);
            payloadValidator.validate(eventRequest);
        } catch (JacksonException | InvalidPayloadException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", messageSource.getMessage("ingest.invalidPayload", null, CurrentLocale.getCurrentLocale())));
        }

        EventMessage message = new EventMessage(projectId, Instant.now(), EventPayloadRedactor.scrub(eventRequest));
        try {
            rabbitTemplate.convertAndSend(CommonConstants.QUEUE, message);
        } catch (AmqpException e) {
            log.error("Failed to enqueue message: {}", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", messageSource.getMessage("ingest.unavailable", null, CurrentLocale.getCurrentLocale())));
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("status", messageSource.getMessage("ingest.queued", null, CurrentLocale.getCurrentLocale())));
    }

    private ResponseEntity<Map<String, String>> tooMany() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "1").body(Map.of("error", messageSource.getMessage("ingest.rateLimited", null, CurrentLocale.getCurrentLocale())));
    }

    private ResponseEntity<Map<String, String>> tooLarge() {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(Map.of("error", messageSource.getMessage("ingest.payloadTooLarge", null, CurrentLocale.getCurrentLocale())));
    }
}
