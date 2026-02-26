package org.example.consumerusermessages1.repo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.example.consumerusermessages1.KafkaUserMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KafkaUserMessageRepository extends JpaRepository<KafkaUserMessageEntity, Long> {

    @Modifying
    @Query(value = """
    INSERT INTO kafka_user_messages (
        topic, partition_id, offset_id, message_key, message_id, user_id, content, status, message_time, outbox_status,
        kafka_ts_ms, received_at, updated_at
    ) VALUES (
        :topic, :partitionId, :offsetId, :messageKey, :messageId, :userId, :content, :status, :messageTime, :outboxStatus,
        :kafkaTimestampMs, :receivedAt, NOW()
    )
    ON CONFLICT (message_key) DO NOTHING
    """, nativeQuery = true)
    int insertWithConflictIgnore(
        @Param("topic") String topic,
        @Param("partitionId") Integer partition,
        @Param("offsetId") long offset,
        @Param("messageKey") String messageKey,
        @Param("messageId") String messageId,
        @Param("userId") Long userId,
        @Param("content") String content,
        @Param("status") String status,
        @Param("messageTime") LocalDateTime messageTime,
        @Param("outboxStatus") String outboxStatus,
        @Param("kafkaTimestampMs") Long kafkaTimestampMs,
        @Param("receivedAt") Instant receivedAt
    );

    /**
     * Находит записи со статусом PENDING и атомарно помечает их как PROCESSING
     * FOR UPDATE SKIP LOCKED — для конкурентной обработки несколькими инстансами
     */
    @Modifying
    @Query(value = """
        UPDATE kafka_user_messages
        SET outbox_status = :newStatus, updated_at = NOW() 
        WHERE id IN (
            SELECT id 
            FROM kafka_user_messages 
            WHERE outbox_status = :currentStatus 
            ORDER BY received_at ASC 
            LIMIT :limit 
            FOR UPDATE SKIP LOCKED
        )
        RETURNING *
        """, nativeQuery = true)
    List<KafkaUserMessageEntity> findPendingAndMarkAsProcessing(
        @Param("currentStatus") String currentStatus,
        @Param("newStatus") String newStatus,
        @Param("limit") int limit
    );

    long countByMessageKey(String messageKey);
}
