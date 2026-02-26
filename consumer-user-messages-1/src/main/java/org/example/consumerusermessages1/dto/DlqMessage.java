package org.example.consumerusermessages1.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DlqMessage {
    private String originalTopic;
    private String originalKey;
    private Object payload;
    private String errorType;
    private String errorMessage;
    private Instant timestamp;
}
