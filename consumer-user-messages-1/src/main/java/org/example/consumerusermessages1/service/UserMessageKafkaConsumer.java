package org.example.consumerusermessages1.service;

import java.util.Arrays;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.protobuf.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Profile({"processing", "finished"})
@Service
@RequiredArgsConstructor
public class UserMessageKafkaConsumer {

    @Value("${app.status-filter}")
    private String statusFilter;

    private final KafkaUserMessageService kafkaUserMessageService;

    @KafkaListener(topics = "user.message.topic", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onMessage(ConsumerRecord<String, byte[]> record, Acknowledgment ack) throws Exception {
        saveMessage(record, ack);
    }

    private void saveMessage(ConsumerRecord<String, byte[]> record, Acknowledgment ack) {
        log.info("Getting message offset = {}", record.offset());
        try {
            final UserMessage userMessage = UserMessage.parseFrom(record.value());
            if (UserMessage.Status.valueOf(statusFilter.toUpperCase()) != userMessage.getStatus()) {
                log.warn("Skipping target status = {}", userMessage.getStatus());
                return;
            }
            kafkaUserMessageService.insertIntoIgnoringConflict(userMessage, record, OutboxStatus.PENDING);
            ack.acknowledge();
            log.info("End of getting message offset = {}", record.offset());
        } catch (Exception other) {
            //TODO A place for Dead Letter Queue pattern
            log.warn("Got exception message for offset={}", record.key(), other);
        }
    }

    // Config validation during app starting
    @PostConstruct
    public void validateStatusFilter() {
        try {
            UserMessage.Status.valueOf(statusFilter.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "Invalid statusFilter config: '" + statusFilter +
                    "'. Allowed values: " + Arrays.toString(UserMessage.Status.values()), e);
        }
    }
}
