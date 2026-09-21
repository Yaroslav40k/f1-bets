package com.sporty.event.outcome.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstraps the REST API that accepts event outcomes and publishes them to Kafka.
 */
@SpringBootApplication
public class OutcomeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutcomeApiApplication.class, args);
    }

}
