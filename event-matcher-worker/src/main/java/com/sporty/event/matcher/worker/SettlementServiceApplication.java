package com.sporty.event.matcher.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bootstraps the matcher worker that consumes outcomes, stores bets, and dispatches outbox events.
 */
@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan("com.sporty.event.matcher.worker.adapter.out.provider")
public class SettlementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettlementServiceApplication.class, args);
    }

}
