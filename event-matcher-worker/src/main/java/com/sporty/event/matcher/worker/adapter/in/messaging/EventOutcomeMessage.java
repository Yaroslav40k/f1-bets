package com.sporty.event.matcher.worker.adapter.in.messaging;

import java.util.UUID;

/**
 * Represents the Kafka message that announces the winner of a sporting event.
 *
 * @param eventId identifier of the completed sporting event.
 * @param eventName display name of the event for logging and traceability.
 * @param eventWinnerId identifier of the winning outcome used for bet matching.
 */
public record EventOutcomeMessage(UUID eventId,
                                  String eventName,
                                  UUID eventWinnerId) {
}
