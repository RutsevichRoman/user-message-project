package org.example.producerservice.service;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.producerservice.MessageType;
import org.example.producerservice.dto.UserMessage;
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
            final UserMessage userMessage = new UserMessage(
                UUID.randomUUID().toString(),
                RANDOM.nextLong(),
                MessageType.TEXT,
                TEXT_MESSAGE.getBytes(),
                Instant.now()
            );

            log.info("Sending user message with id = {}", userMessage.getId());
            userMessageProducerService.sendMessage(userMessage);
        }
    }

}
