package com.sentry.backend.dto.request;

public record BreadcrumbRequest(long timestamp, String message) {}
