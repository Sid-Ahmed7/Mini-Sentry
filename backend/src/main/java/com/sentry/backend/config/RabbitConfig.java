package com.sentry.backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sentry.backend.constants.CommonConstants;

import tools.jackson.databind.json.JsonMapper;

@Configuration 
public class RabbitConfig {
    
    @Bean 
    Queue eventQueue() {
        return QueueBuilder.durable(CommonConstants.QUEUE)
            .deadLetterExchange(CommonConstants.DEAD_EXCHANGE)
            .deadLetterRoutingKey(CommonConstants.DEAD_KEY)
            .maxLength(CommonConstants.QUEUE_MAX_LENGTH)
            .build();
    }

    @Bean 
    Queue deadQueue() {
        return  QueueBuilder.durable(CommonConstants.DEAD_QUEUE)
            .ttl(CommonConstants.QUEUE_TTL)
            .maxLength(CommonConstants.QUEUE_MAX_LENGTH)
            .build();
    }

    @Bean
    Binding deadBinding(Queue deadQueue, DirectExchange deadExchange) {
        return BindingBuilder.bind(deadQueue).to(deadExchange).with(CommonConstants.DEAD_KEY);
    }

    @Bean
    MessageConverter messageConverter(JsonMapper mapper) {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(mapper);
        converter.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }
}