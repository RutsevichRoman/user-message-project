package org.example.consumerusermessages1.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.consumerusermessages1.repo.KafkaUserMessageRepository;
import org.example.protobuf.UserMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaUserMessageService {

    private final KafkaUserMessageRepository kafkaUserMessageRepository;

    public int insertIntoIgnoringConflict(UserMessage userMessage, ConsumerRecord<String, byte[]> record, OutboxStatus pending) {
        return kafkaUserMessageRepository.insertWithConflictIgnore(
            record.topic(), record.partition(), record.offset(), record.key(), userMessage.getId(),
            userMessage.getUserId(), userMessage.getContent(), userMessage.getStatus().name(),
            LocalDateTime.ofInstant(Instant.ofEpochMilli(userMessage.getTimestampMs()), TimeZone.getDefault().toZoneId()),
            pending.name(), record.timestamp(), Instant.now()
        );
    }
}
