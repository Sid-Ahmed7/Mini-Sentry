package com.sentry.backend.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.sentry.backend.config.RabbitConfig;
import com.sentry.backend.constants.CommonConstants;
import com.sentry.backend.services.eventProcessing.EventProcessingService;

@Component 
public class EventQueueListener {
    
    private final EventProcessingService eventProcessingService;

    public EventQueueListener(EventProcessingService eventProcessingService) {
        this.eventProcessingService = eventProcessingService;
    }

    @RabbitListener(queues = CommonConstants.QUEUE)
    public void onEvent(EventMessage eventMessage) {
        eventProcessingService.process(eventMessage);
    }
}
