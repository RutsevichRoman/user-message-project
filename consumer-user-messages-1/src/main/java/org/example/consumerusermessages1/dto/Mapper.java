package org.example.consumerusermessages1.dto;

import org.example.consumerusermessages1.KafkaUserMessageEntity;

public class Mapper {

    public static MessageSavedEvent fromEntity(KafkaUserMessageEntity entity) {
        return new MessageSavedEvent(
            entity.getMessageKey(),
            entity.getStatus(),
            entity.getReceivedAt(),
            entity.getId(),
            entity.getTopic(),
            entity.getOffset()
        );
    }
}
