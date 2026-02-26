package org.example.consumerusermessages1;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.consumerusermessages1.repo.KafkaUserMessageRepository;
import org.example.consumerusermessages1.service.UserMessageKafkaConsumer;
import org.example.protobuf.UserMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.Acknowledgment;

class UserMessageKafkaConsumerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaUserMessageRepository kafkaUserMessageRepository;

    @Autowired
    private UserMessageKafkaConsumer userMessageKafkaConsumer;

    @BeforeEach
    void cleanDatabase() {
        kafkaUserMessageRepository.deleteAll();
    }

    @Test
    @DisplayName("Repeat ack with the same key")
    void test_shouldHandleDuplicateMessagesIdempotently() throws Exception {
        // Given: два одинаковых сообщения с одним ключом
        String key = "user-123-order-456";
        UserMessage message = createUserMessageTest();

        ConsumerRecord<String, byte[]> record1 = createRecord(key, message);
        ConsumerRecord<String, byte[]> record2 = createRecord(key, message);

        Acknowledgment ack1 = createMockAck();
        Acknowledgment ack2 = createMockAck();

        // When: обрабатываем параллельно (имитируем гонку условий)
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            try {
                userMessageKafkaConsumer.onMessage(record1, ack1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            try {
                userMessageKafkaConsumer.onMessage(record2, ack2);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Ждём завершения обоих потоков
        CompletableFuture.allOf(future1, future2).get(5, TimeUnit.SECONDS);

        // Then: оба сообщения подтверждены (идемпотентность)
        verify(ack1, times(1)).acknowledge();
        verify(ack2, times(1)).acknowledge();

        //  Сохранился только одна запись из двух с один и тем же ключом
        final long countByKey = kafkaUserMessageRepository.countByMessageKey(key);
        assertEquals(1, countByKey);
    }

    /**
     * тестовое protobuf-сообщение
     */
    private UserMessage createUserMessageTest() {
        return  UserMessage.newBuilder()
            .setId("msg-" + UUID.randomUUID())
            .setUserId(1L)
            .setContent("User action: payment initiated")
            .setStatus(UserMessage.Status.PROCESSING)
            .setTimestampMs(System.currentTimeMillis())
            .build();
    }

    /**
     * Создание ConsumerRecord для тестов
     */
    private ConsumerRecord<String, byte[]> createRecord(String key, UserMessage message) {
        return new ConsumerRecord<>(
            "user.message.topic",  // topic
            0,                     // partition
            123L,                  // offset
            key,                   // key
            message.toByteArray()  // value (protobuf → bytes)
        );
    }

    /**
     * Мок для Acknowledgment (для тестов)
     */
    private Acknowledgment createMockAck() {
        return mock(Acknowledgment.class);
    }
}
