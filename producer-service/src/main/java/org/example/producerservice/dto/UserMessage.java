package org.example.producerservice.dto;

import java.io.Serializable;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.producerservice.MessageType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserMessage implements Serializable {
    private String id;
    private Long userId;
    private MessageType messageType;
    // fixed type to use ByteArraySerializer
    private byte[] content;
    private Instant timestamp;
}
