package com.sentry.backend.messaging;

import java.time.Instant;
import java.util.UUID;

import com.sentry.backend.dto.request.EventRequest;

public record EventMessage(UUID projectId, Instant receivedAt, EventRequest eventRequest) {}