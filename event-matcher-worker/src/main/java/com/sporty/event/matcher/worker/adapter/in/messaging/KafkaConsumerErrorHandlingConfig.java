package com.sporty.event.matcher.worker.adapter.in.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Bounds how long a failing {@code event-outcomes} record is retried. Spring Boot auto-wires any
 * {@link CommonErrorHandler} bean into the default {@code ConcurrentKafkaListenerContainerFactory},
 * so no manual container factory wiring is required.
 *
 * <p>Combined with the {@code ErrorHandlingDeserializer} configured in
 * {@code application.properties}, a record that fails to deserialize (or whose listener throws)
 * is retried twice with a 1-second delay and then logged and skipped, instead of blocking the
 * partition forever by seeking back to the same offset indefinitely.
 */
@Configuration
public class KafkaConsumerErrorHandlingConfig {

    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(1000L, 2L));
    }

}
