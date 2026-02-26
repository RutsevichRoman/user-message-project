package org.example.kafka.producer.starter;

import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProducerTemplate {

    private final KafkaTemplate<String, Object> kafkaByteTemplate;
    private final KafkaTemplate<String, String> kafkaStringTemplate;

    /**
     * Асинхронная отправка bytes
     */
    public void sendBytesMessageToKafka(String topic, String key, byte[] bytes) {
        kafkaByteTemplate.send(topic, key, bytes)
            .whenComplete((res, ex) -> {
                if (ex != null) {
                    log.info("FAILED: {}", ex.getMessage());
                } else {
                    log.info("SENT to {} partition={} offset={}", res.getRecordMetadata().topic(),
                        res.getRecordMetadata().partition(), res.getRecordMetadata().offset());
                }
            });
    }

    /**
     * Асинхронная отправка String
     */
    public CompletableFuture<SendResult<String, String>> sendStringMessageToKafka(String topic, String key, String event) {
        return kafkaStringTemplate.send(topic, key, event);
    }

    /**
     * Асинхронная отправка String into DLQ
     */
    public void sendStringDlqToKafka(String topic, String key, String json) {
        kafkaStringTemplate.send(topic, key, json)
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
