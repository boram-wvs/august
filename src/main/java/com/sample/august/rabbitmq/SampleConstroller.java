package com.sample.august.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class SampleConstroller {
    private final StreamBridge streamBridge;
    private final AsyncRabbitTemplate asyncRabbitTemplate;

    @GetMapping(value = "/message/{message}")
    public ResponseEntity<Boolean> endpoint0(@PathVariable String message) {
        return ResponseEntity.ok(streamBridge.send("processor-in-0", MessageBuilder.withPayload(message).build()));
    }

    @GetMapping(value = "/message/rpc/{message}")
    public ResponseEntity<Boolean> endpoint2(@PathVariable String message) throws ExecutionException, InterruptedException {

        Future<String> result = asyncRabbitTemplate.convertSendAndReceive("sample-direct-0","sample-queue-4", SampleMessage.builder().body(message).build());
        String sampleMessage =  result.get();
        log.info("RESULT : {} ", sampleMessage);

        return ResponseEntity.ok(Boolean.TRUE);
    }
}
