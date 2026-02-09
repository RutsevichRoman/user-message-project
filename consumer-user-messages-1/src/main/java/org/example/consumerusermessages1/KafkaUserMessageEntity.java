package org.example.consumerusermessages1;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "kafka_user_message",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_topic_partition_offset",
        columnNames = {"topic", "partition_id", "offset_id"}
    )
)
@NoArgsConstructor
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
    private String key;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private byte[] message;

    @Column(name = "kafka_ts_ms")
    private Long kafkaTimestampMs;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    public KafkaUserMessageEntity(String topic, int partition, long offset, String key, byte[] message, Long kafkaTimestampMs,
                                  Instant receivedAt) {
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.key = key;
        this.message = message;
        this.kafkaTimestampMs = kafkaTimestampMs;
        this.receivedAt = receivedAt;
    }
}
