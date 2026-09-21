package com.sporty.event.matcher.worker.adapter.out.messaging.outbox;

import com.sporty.event.matcher.worker.application.outbox.OutboxPayloadSerializer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Serializes outbox payload objects to JSON before they are persisted.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class JacksonOutboxPayloadSerializer implements OutboxPayloadSerializer {

    ObjectMapper objectMapper;

    /**
     * Converts the supplied payload object into its JSON representation.
     *
     * @param payload domain payload to persist in the outbox row.
     * @return serialized JSON body for the outbox entry.
     * @throws IllegalStateException if the payload cannot be serialized.
     */
    @Override
    public String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize outbox payload", ex);
        }
    }
}
