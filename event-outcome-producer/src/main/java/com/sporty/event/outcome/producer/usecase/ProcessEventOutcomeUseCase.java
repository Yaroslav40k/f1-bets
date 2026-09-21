package com.sporty.event.outcome.producer.usecase;

import com.sporty.event.outcome.producer.adapter.out.messaging.EventOutcomeMessage;
import com.sporty.event.outcome.producer.adapter.out.messaging.EventOutcomePublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

/**
 * Coordinates publication of an event outcome received through the REST API.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProcessEventOutcomeUseCase {

    EventOutcomePublisher eventOutcomePublisher;

    /**
     * Forwards the received event outcome to the messaging adapter.
     *
     * @param message event outcome accepted by the API layer.
     */
    public void publishEventOutcome(EventOutcomeMessage message) {
        eventOutcomePublisher.publish(message);
    }

}
