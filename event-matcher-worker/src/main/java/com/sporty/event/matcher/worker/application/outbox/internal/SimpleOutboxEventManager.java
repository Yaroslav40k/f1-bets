package com.sporty.event.matcher.worker.application.outbox.internal;

import com.sporty.event.matcher.worker.adapter.out.messaging.outbox.OutboxPublisherException;
import com.sporty.event.matcher.worker.application.outbox.OutboxEventManager;
import com.sporty.event.matcher.worker.application.outbox.OutboxEventRepository;
import com.sporty.event.matcher.worker.application.outbox.OutboxMessagePublisher;
import com.sporty.event.matcher.worker.application.outbox.OutboxPayloadSerializer;
import com.sporty.event.matcher.worker.application.outbox.OutboxPropertiesProvider;
import com.sporty.event.matcher.worker.application.outbox.model.OutboxEvent;
import com.sporty.event.matcher.worker.application.outbox.model.OutboxEventCommand;
import com.sporty.event.matcher.worker.application.outbox.model.OutboxEventStatus;
import com.sporty.event.matcher.worker.application.outbox.model.OutboxPayloadType;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implements the transactional outbox flow: append events in the business transaction, then
 * publish and mark them as published or failed during scheduled dispatch.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
class SimpleOutboxEventManager implements OutboxEventManager {

    OutboxEventRepository outboxEventRepository;
    OutboxPayloadSerializer outboxPayloadSerializer;
    OutboxMessagePublisher outboxMessagePublisher;
    OutboxPropertiesProvider outboxPropertiesProvider;

    /**
     * Persists a new outbox event using the supplied command data.
     *
     * @param outboxEventCommand command describing the event to append.
     */
    @Override
    public void append(OutboxEventCommand outboxEventCommand) {
        var outboxEvent = new OutboxEvent(
                UUID.randomUUID(),
                outboxEventCommand.aggregateId(),
                outboxEventCommand.topic(),
                outboxEventCommand.partitionKey(),
                outboxPayloadSerializer.serialize(outboxEventCommand.payload()),
                OutboxEventStatus.PENDING
        );
        outboxEventRepository.save(outboxEvent);
    }

    /**
     * Publishes the next pending outbox event and records whether publication succeeded.
     *
     * @return {@code true} when a pending event was processed, otherwise {@code false}.
     */
    @Override
    @Transactional
    public boolean dispatchNext() {
        var maxRetries = outboxPropertiesProvider.getMaxRetries();
        var pendingEvent = outboxEventRepository.fetchNextPending(maxRetries);
        var isPendingEventEmpty = pendingEvent.isEmpty();
        if (isPendingEventEmpty) {
            return false;
        }

        var outboxRecord = pendingEvent.get();
        try {
            outboxMessagePublisher.publish(outboxRecord);
            outboxEventRepository.markPublished(outboxRecord.id());
            log.debug("Outbox event published: id=[{}], topic=[{}]", outboxRecord.id(), outboxRecord.topic());
            return true;
        } catch (OutboxPublisherException ex) {
            log.warn("Failed to publish outbox event id=[{}]: [{}]", outboxRecord.id(), ex.getMessage(), ex);
            outboxEventRepository.markFailed(outboxRecord.id(), ex.getMessage());
            return true;
        }
    }

    /**
     * Resolves the configured topic for the supplied payload type.
     *
     * @param ruleType payload type whose topic should be resolved.
     * @return configured topic name for the payload type.
     */
    @Override
    public String getTopicByPayloadType(OutboxPayloadType ruleType) {
        return outboxPropertiesProvider.getTopicByRuleType(ruleType);
    }

    /**
     * Returns the number of outbox events that may be processed in one dispatch loop.
     *
     * @return configured dispatch batch size.
     */
    @Override
    public int getBatchSize() {
        return outboxPropertiesProvider.getBatchSize();
    }

}
