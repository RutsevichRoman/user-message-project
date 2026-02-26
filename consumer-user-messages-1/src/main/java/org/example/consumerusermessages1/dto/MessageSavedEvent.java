package org.example.consumerusermessages1.dto;

import java.time.Instant;

public record MessageSavedEvent(
    String messageKey,
    String status,
    Instant savedAt,
    Long dbId,
    String sourceTopic,
    Long sourceOffset
) {}