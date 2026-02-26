package org.example.consumerusermessages1;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.protobuf.UserMessage;

@Entity
@Table(
    name = "kafka_user_messages",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_message_key",
        columnNames = {"message_key"}
    )
)
@NoArgsConstructor
@Getter
@Setter
public class KafkaUserMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Column(name = "partition_id", nullable = false)
    private int partition;

    @Column(name = "offset_id", nullable = false)
    private long offset;

    @Column(name = "message_key")
    private String messageKey;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "content")
    private String content;

    @Column(name = "status")
    private String status;

    @Column(name = "message_time")
    private LocalDateTime messageTime;

    @Column(name = "outbox_status")
    private String outboxStatus;

    @Column(name = "kafka_ts_ms")
    private Long kafkaTimestampMs;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public KafkaUserMessageEntity(String topic, int partition, long offset, String messageKey, UserMessage userMessage,
                                  String outboxStatus, Long kafkaTimestampMs, Instant receivedAt) {
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.messageKey = messageKey;

        this.messageId = userMessage.getId();
        this.userId = userMessage.getUserId();
        this.status = userMessage.getStatus().name();
        this.content = userMessage.getContent();
        this.messageTime = Instant.ofEpochMilli(userMessage.getTimestampMs())
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
        this.outboxStatus = outboxStatus;

        this.kafkaTimestampMs = kafkaTimestampMs;
        this.receivedAt = receivedAt;
    }
}
