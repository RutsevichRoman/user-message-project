package org.example.consumerusermessages1.repo;

import org.example.consumerusermessages1.KafkaUserMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KafkaUserMessageRepository extends JpaRepository<KafkaUserMessageEntity, Long> {
}
