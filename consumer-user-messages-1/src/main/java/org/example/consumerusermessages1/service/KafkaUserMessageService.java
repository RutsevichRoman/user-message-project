package org.example.consumerusermessages1.service;

import lombok.RequiredArgsConstructor;
import org.example.consumerusermessages1.KafkaUserMessageEntity;
import org.example.consumerusermessages1.repo.KafkaUserMessageRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaUserMessageService {

    private final KafkaUserMessageRepository kafkaUserMessageRepository;

    public KafkaUserMessageEntity store(KafkaUserMessageEntity entity) {
        final KafkaUserMessageEntity save = kafkaUserMessageRepository.save(entity);
        kafkaUserMessageRepository.flush();
        return save;
    }

}
