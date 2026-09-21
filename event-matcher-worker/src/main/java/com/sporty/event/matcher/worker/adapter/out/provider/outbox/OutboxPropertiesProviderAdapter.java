package com.sporty.event.matcher.worker.adapter.out.provider.outbox;

import com.sporty.event.matcher.worker.application.outbox.OutboxPropertiesProvider;
import com.sporty.event.matcher.worker.application.outbox.model.OutboxPayloadType;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

/**
 * Adapts bound Spring configuration properties to the application outbox settings port.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
class OutboxPropertiesProviderAdapter implements OutboxPropertiesProvider {

    OutboxProperties outboxProperties;

    /**
     * Resolves the configured topic for the supplied outbox payload type.
     *
     * @param ruleType payload type whose topic should be returned.
     * @return configured topic name for the payload type.
     */
    @Override
    public String getTopicByRuleType(OutboxPayloadType ruleType) {
        return outboxProperties.topicFor(ruleType);
    }

    /**
     * Returns the configured retry limit for pending outbox events.
     *
     * @return maximum number of publish attempts before the event stops being retried.
     */
    @Override
    public int getMaxRetries() {
        return outboxProperties.getDispatch().getMaxRetries();
    }

    /**
     * Returns the maximum number of outbox events to process per scheduler tick.
     *
     * @return configured dispatch batch size.
     */
    @Override
    public int getBatchSize() {
        return outboxProperties.getDispatch().getBatchSize();
    }

    /**
     * Returns the configured broker send timeout.
     *
     * @return send timeout in milliseconds.
     */
    @Override
    public long getSendTimeoutMs() {
        return outboxProperties.getDispatch().getSendTimeoutMs();
    }

}
