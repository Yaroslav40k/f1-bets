package com.sporty.event.outcome.producer.adapter.out.messaging;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes event outcome messages to Kafka for downstream matching.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventOutcomePublisher {

    final KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate;

    @Value("${event-outcome-producer.messaging.event.outcome.topic}")
    String topic;

    /**
     * Sends the supplied event outcome to the configured Kafka topic.
     *
     * @param message event outcome to publish for downstream processing.
     */
    public void publish(EventOutcomeMessage message) {
        kafkaTemplate.send(topic, message);
    }

}
