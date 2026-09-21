package com.sporty.event.matcher.worker.application.outbox.model;

/**
 * Enumerates lifecycle states of a persisted transactional outbox event.
 */
public enum OutboxEventStatus {
    PENDING,
    PUBLISHED
}
