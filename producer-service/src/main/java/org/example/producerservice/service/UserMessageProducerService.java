package org.example.producerservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.producerservice.dto.UserMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMessageProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(UserMessage userMessage) {
        kafkaTemplate.send("user.message.topic", userMessage.getId(), userMessage)
            .whenComplete((res, ex) -> {
            if (ex != null) {
                log.info("FAILED: {}", ex.getMessage());
            } else {
                log.info("SENT to {} partition={} offset={}", res.getRecordMetadata().topic(),
                    res.getRecordMetadata().partition(), res.getRecordMetadata().offset());
            }
        });
    }
}
