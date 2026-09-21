package com.sporty.event.outcome.producer.adapter.out.messaging;

import java.util.UUID;

/**
 * Represents the event outcome payload sent to the {@code event-outcomes} Kafka topic.
 *
 * @param eventId identifier of the sporting event that has completed.
 * @param eventName display name of the event for downstream logging and traceability.
 * @param eventWinnerId identifier of the winning outcome for the event.
 */
public record EventOutcomeMessage(UUID eventId,
                                  String eventName,
                                  UUID eventWinnerId) {
}
