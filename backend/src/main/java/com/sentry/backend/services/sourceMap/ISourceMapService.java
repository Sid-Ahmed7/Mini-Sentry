package com.sentry.backend.services.sourceMap;

import java.util.UUID;

import com.sentry.backend.dto.request.EventRequest;

public interface ISourceMapService {
    
    void validate(String json);

    void invalidate(UUID projectId, String release, String file);

    EventRequest apply(UUID projectId, EventRequest event);
}
