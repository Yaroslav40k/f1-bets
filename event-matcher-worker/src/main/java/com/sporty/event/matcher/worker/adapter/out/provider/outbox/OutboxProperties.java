package com.sporty.event.matcher.worker.adapter.out.provider.outbox;

import com.sporty.event.matcher.worker.application.outbox.model.OutboxPayloadType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.EnumMap;
import java.util.Map;

/**
 * Binds transactional outbox configuration, including topic routing and dispatch limits.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "event-matcher-worker.outbox")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
class OutboxProperties {

    @Valid
    DispatchProperties dispatch = new DispatchProperties();

    @NotEmpty
    Map<OutboxPayloadType, String> walletTopics = new EnumMap<>(OutboxPayloadType.class);

    /**
     * Resolves the broker topic configured for the supplied outbox payload type.
     *
     * @param ruleType payload type whose topic should be resolved.
     * @return configured topic name for the payload type.
     * @throws IllegalArgumentException if no topic is configured for the payload type.
     */
    public String topicFor(OutboxPayloadType ruleType) {
        String topic = walletTopics.get(ruleType);
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("No Kafka topic configured for rule type: " + ruleType);
        }
        return topic;
    }

    /**
     * Represents scheduler-level settings that control how outbox events are dispatched.
     */
    @Getter
    @Setter
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class DispatchProperties {

        int maxRetries = 5;
        int batchSize = 100;
        long sendTimeoutMs = 30_000;

    }

}

