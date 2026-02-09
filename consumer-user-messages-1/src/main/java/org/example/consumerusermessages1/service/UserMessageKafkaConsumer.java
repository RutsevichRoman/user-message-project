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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Profile({"consumer1", "consumer2"})
@Service
@RequiredArgsConstructor
public class UserMessageKafkaConsumer {

    private final KafkaUserMessageService kafkaUserMessageService;

    @KafkaListener(topics = "user.message.topic", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onMessage(ConsumerRecord<String, byte[]> record, Acknowledgment ack) {
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

            // ACK только после успешного COMMIT БД
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    ack.acknowledge();
                    log.info("ACK after DB commit offset={}", record.offset());
                }
            });
            log.info("End of getting message offset = {}", record.offset());
        } catch (DataIntegrityViolationException duplicate) {
            // Дубликат — можно ack после коммита (чтобы не ack-нуть, если транзакция всё же откатится)
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    ack.acknowledge();
                    log.info("ACK duplicate after DB commit offset={}", record.offset());
                }
            });

            log.warn("Duplicate message (already stored) offset={}", record.offset(), duplicate);
        } catch (Exception other) {
            //TODO A place for Dead Letter Queue pattern
            log.warn("Got exception message for offset={}", record.key(), other);
        }
    }
}
