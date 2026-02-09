package org.example.producerservice.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

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
        try {
            sendMessageToKafka(userMessage);
        } catch (IOException e) {
            log.warn("Error sending message to kafka", e);
        }
    }

    private void sendMessageToKafka(UserMessage userMessage) throws IOException {
        kafkaTemplate.send("user.message.topic", userMessage.getId(), getBytes(userMessage))
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
     * Converts a Serializable object to a byte array.
     * @param obj The object to convert.
     * @return The byte array representation of the object.
     * @throws IOException If an I/O error occurs during serialization.
     */
    public byte[] getBytes(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            oos.flush(); // Ensure all data is written to the stream
            return bos.toByteArray();
        }
    }
}
