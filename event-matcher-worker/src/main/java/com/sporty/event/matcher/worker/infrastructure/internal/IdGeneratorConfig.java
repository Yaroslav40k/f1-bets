package com.sporty.event.matcher.worker.infrastructure.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.IdGenerator;
import org.springframework.util.JdkIdGenerator;

/**
 * Registers the {@code IdGenerator} used for work-unit and outbox identifiers.
 */
@Configuration
public class IdGeneratorConfig {

    /**
     * Creates the UUID-based identifier generator used by the matcher workflow.
     *
     * @return {@code JdkIdGenerator} bean for generating unique identifiers.
     */
    @Bean
    public IdGenerator idGenerator() {
        return new JdkIdGenerator();
    }
}
