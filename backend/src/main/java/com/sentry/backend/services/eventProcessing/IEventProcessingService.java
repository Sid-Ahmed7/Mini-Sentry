package com.sentry.backend.services.eventProcessing;

import com.sentry.backend.messaging.EventMessage;

public interface IEventProcessingService {
    void process(EventMessage message);

    
}