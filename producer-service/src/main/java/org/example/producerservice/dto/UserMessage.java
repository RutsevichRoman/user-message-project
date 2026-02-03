package org.example.producerservice.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.producerservice.MessageType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserMessage {
    private String id;
    private Long userId;
    private MessageType messageType;
    private byte[] content;
    private Instant timestamp;
}
