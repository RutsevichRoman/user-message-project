package org.example.consumerusermessages1.service;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    FAILED,
    PUBLISHED
}
