package org.example.consumerusermessages1.service;

import java.time.Instant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.consumerusermessages1.KafkaUserMessageEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Profile({"consumer1", "consumer2"})
@Service
@RequiredArgsConstructor
public class UserMessageKafkaConsumer {

    private final KafkaUserMessageService kafkaUserMessageService;

    @KafkaListener(topics = "user.message.topic", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("Getting message offset = {}", record.offset());
        try {
            var entity = new KafkaUserMessageEntity(
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                record.value(),
                record.timestamp(),
                Instant.now()
            );

            kafkaUserMessageService.store(entity);

            // коммитим offset только если БД успешно записала
            ack.acknowledge();
            log.info("End of getting message offset = {}", record.offset());
        } catch (DataIntegrityViolationException duplicate) {
            // если пришёл дубль (topic+partition+offset уже есть) — считаем обработанным
            log.error("End of getting message offset = {}", record.offset(), duplicate);
            ack.acknowledge();
        }
    }
}
