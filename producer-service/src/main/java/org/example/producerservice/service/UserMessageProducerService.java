package org.example.producerservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kafka.producer.starter.KafkaProducerTemplate;
import org.example.protobuf.UserMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMessageProducerService {

    private final KafkaProducerTemplate kafkaProducerTemplate;

    public void sendNewMessage(UserMessage userMessage) {
        kafkaProducerTemplate.sendBytesMessageToKafka("user.message.topic", userMessage.getId(), userMessage.toByteArray());
    }
}
