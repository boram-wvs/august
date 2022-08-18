package com.sample.august.rabbitmq;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
@Slf4j
public class SampleService {

    private final AmqpTemplate amqpTemplate;

    @Bean
    public Function<String, String> processor() {
        return value -> {
            log.info("[{}]{}", Thread.currentThread().getName(), value);
            return value.toUpperCase();
        };
    }

    @Bean
    public Consumer<String> consumer() {
        return value -> log.info("[{}]{}", Thread.currentThread().getName(), value);
    }

    @Bean
    public Consumer<Message<SampleMessage>> rpcconsumer() {
        RabbitTemplate template = new RabbitTemplate();

        return message -> {
            message.getHeaders().forEach((k, v) -> log.info("{} = {}", k, v));
            amqpTemplate.convertAndSend((String) message.getHeaders().get(AmqpHeaders.REPLY_TO), message.getHeaders().get(AmqpHeaders.CORRELATION_ID),
                    messagePostProcessor -> {
                        messagePostProcessor.getMessageProperties().setCorrelationId((String) message.getHeaders().get(AmqpHeaders.CORRELATION_ID));
                        return messagePostProcessor;
                    });
        };
    }
}
