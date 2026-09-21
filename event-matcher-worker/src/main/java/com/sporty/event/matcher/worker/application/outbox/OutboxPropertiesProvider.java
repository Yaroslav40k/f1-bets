package com.sporty.event.matcher.worker.application.outbox;

import com.sporty.event.matcher.worker.application.outbox.model.OutboxPayloadType;

/**
 * Supplies resolved outbox configuration values to application services.
 */
public interface OutboxPropertiesProvider {

    /**
     * Resolves the topic configured for the supplied payload type.
     *
     * @param ruleType payload type whose topic should be returned.
     * @return configured topic name for the payload type.
     */
    String getTopicByRuleType(OutboxPayloadType ruleType);

    /**
     * Returns the maximum number of times a pending outbox event may be retried.
     *
     * @return configured retry limit.
     */
    int getMaxRetries();

    /**
     * Returns the number of outbox events that may be dispatched per scheduler tick.
     *
     * @return configured dispatch batch size.
     */
    int getBatchSize();

    /**
     * Returns the configured broker send timeout in milliseconds.
     *
     * @return send timeout in milliseconds.
     */
    long getSendTimeoutMs();
}
