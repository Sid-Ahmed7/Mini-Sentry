package com.sentry.backend.dto.request;

public record FrameRequest(String function, String file, Integer line, Integer column, Boolean inApp) {}
