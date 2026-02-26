package org.example.consumerusermessages1.scheduler;

import java.util.List;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.consumerusermessages1.KafkaUserMessageEntity;
import org.example.consumerusermessages1.dto.DlqMessage;
import org.example.consumerusermessages1.dto.Mapper;
import org.example.consumerusermessages1.dto.MessageSavedEvent;
import org.example.consumerusermessages1.repo.KafkaUserMessageRepository;
import org.example.consumerusermessages1.service.OutboxStatus;
import org.example.kafka.producer.starter.KafkaProducerTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingPaymentOutbox {

    private static final String USER_MESSAGE_TOPIC_SAVED = "user.message.saved";
    private static final String DLQ_OUTBOX_TOPIC = "payment-outbox.dlq";
    private static final String ERROR_OUTBOX_PROCESSING = "OUTBOX_PROCESSING_FAILED";
    private static final int batchSize = 50;

    private final KafkaUserMessageRepository messageRepository;
    private final KafkaProducerTemplate kafkaProducerTemplate;

    /**
     * Бесконечный цикл обработки: берём батчами записи со статусом PENDING
     */
    @Scheduled(fixedDelayString = "${outbox.fixed-delay:1000}")
    @Transactional
    public void publishPendingEvents() {
        try {
            // 1. Атомарно выбираем и помечаем записи как PROCESSING
            List<KafkaUserMessageEntity> batch = messageRepository.findPendingAndMarkAsProcessing(
                OutboxStatus.PENDING.name(),
                OutboxStatus.PROCESSING.name(),
                batchSize
            );

            if (batch.isEmpty()) {
                log.warn("No records for processing");
                return;
            }

            log.info("📤 Publishing batch of {} MessageSavedEvent(s)", batch.size());

            // 2. Обрабатываем каждую запись
            for (KafkaUserMessageEntity entity : batch) {
                publishSingleEvent(entity);
            }

            // TODO: 3. Metrics ?
        } catch (Exception e) {
            log.error("💥 Unexpected error during outbox publishing", e);
        }
    }

    /**
     * Публикация одного события из сущности
     */
    private void publishSingleEvent(KafkaUserMessageEntity entity) {
        try {
            MessageSavedEvent messageSavedEvent = Mapper.fromEntity(entity);

            // Отправляем в топик
            kafkaProducerTemplate.sendStringMessageToKafka(
                USER_MESSAGE_TOPIC_SAVED,
                entity.getMessageKey(),
                messageSavedEvent.toString()
            ).whenComplete((res, ex) -> {
                if (ex != null) {
                    log.info("FAILED: {}", ex.getMessage());
                } else {
                    // ✅ Помечаем как успешно опубликованную
                    log.info("SENT to {} partition={} offset={}, data = {}", res.getRecordMetadata().topic(),
                        res.getRecordMetadata().partition(), res.getRecordMetadata().offset(), messageSavedEvent);
                    entity.setOutboxStatus(OutboxStatus.PUBLISHED.name());
                    messageRepository.save(entity);
                }
            });

            log.debug("✅ Published MessageSavedEvent key={} status={}",
                entity.getMessageKey(), entity.getStatus());

        } catch (Exception e) {
            handlePublishingError(entity, e);
        }
    }

    /**
     * Обработка ошибки публикации
     */
    private void handlePublishingError(KafkaUserMessageEntity entity, Exception e) {
        String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();

        entity.setOutboxStatus(OutboxStatus.FAILED.name());
        messageRepository.save(entity);

        log.error("❌ Max retries exceeded for event key={} status={}. Error: {}",
            entity.getMessageKey(), entity.getStatus(), errorMessage);

        // Отправка в DLQ (опционально)
        sendToDeadLetterQueue(entity, errorMessage);
    }


    /**
     * Отправка в DLQ при неудачной обработке
     */
    private void sendToDeadLetterQueue(KafkaUserMessageEntity entity, String errorMessage) {
        try {
            // Формируем сообщение для DLQ
            var dlqMessage = new DlqMessage(
                entity.getTopic(),
                entity.getMessageKey(),
                entity.getContent(),
                ERROR_OUTBOX_PROCESSING,
                errorMessage,
                entity.getReceivedAt()
            );

            kafkaProducerTemplate.sendStringDlqToKafka(
                DLQ_OUTBOX_TOPIC,
                entity.getMessageKey(),
                dlqMessage.toString()
            );

            log.warn("📤 Sent failed outbox record to DLQ: id={}", entity.getId());

        } catch (Exception dlqEx) {
            log.error("🔥 Failed to send record to DLQ: id={}", entity.getId(), dlqEx);
        }
    }

}
