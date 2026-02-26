package org.example.producerservice.service;

import java.util.Random;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.protobuf.UserMessage;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("producer")
@Component
@RequiredArgsConstructor
public class UserMessageScheduler {

    private static final String TEXT_MESSAGE = "Hey! How are you ?";
    private static final int BATCH_MESSAGES = 5;
    private static final Random RANDOM = new Random();
    private final UserMessageProducerService userMessageProducerService;

    @Scheduled(fixedRate = 10_000)
    public void sendUserMessage() {
        for (int i = 0; i < BATCH_MESSAGES; i++) {

            final long value = RANDOM.nextLong();
            UserMessage message = UserMessage.newBuilder()
                .setId("msg-" + UUID.randomUUID())
                .setUserId(value)
                .setContent("User action: payment initiated")
                // Setting different statuses: example - use mod
                .setStatus(value % 2 == 0 ? UserMessage.Status.FINISHED : UserMessage.Status.PROCESSING)
                .setTimestampMs(System.currentTimeMillis())
                .build();

            log.info("Sending user message with id = {}", message.getId());
            userMessageProducerService.sendNewMessage(message);
        }
    }
}
