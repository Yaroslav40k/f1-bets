package com.sporty.event.matcher.worker.adapter.out.messaging.outbox;

import com.sporty.event.matcher.worker.application.outbox.OutboxMessagePublisher;
import com.sporty.event.matcher.worker.application.outbox.model.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Publishes outbox events to RocketMQ and uses the outbox id as the broker message key.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class OutboxMessagePublisherAdapter implements OutboxMessagePublisher {

    private final RocketMQTemplate rocketMQTemplate;

    /**
     * Sends a persisted outbox event to RocketMQ and fails if the broker does not acknowledge it.
     *
     * @param outboxEvent persisted outbox event to publish.
     * @throws OutboxPublisherException if publication fails or the broker rejects the send.
     */
    @Override
    public void publish(OutboxEvent outboxEvent) {
        SendResult result;
        try {
            var message = MessageBuilder.withPayload(outboxEvent.payload())
                    .setHeader(RocketMQHeaders.KEYS, outboxEvent.id().toString())
                    .build();
            result = rocketMQTemplate.syncSendOrderly(
                    outboxEvent.topic(), message, outboxEvent.partitionKey());
        } catch (Exception ex) {
            throw new OutboxPublisherException(outboxEvent.id(), outboxEvent.topic(), ex);
        }

        if (result.getSendStatus() != SendStatus.SEND_OK) {
            throw new OutboxPublisherException(outboxEvent.id(), outboxEvent.topic(),
                    new IllegalStateException("Broker returned send status " + result.getSendStatus()));
        }
        log.debug("Published outbox event [{}] to topic [{}] as message [{}]",
                outboxEvent.id(), outboxEvent.topic(), result.getMsgId());
    }

}
