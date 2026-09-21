package com.sporty.event.outcome.producer;

import com.sporty.event.outcome.producer.usecase.ProcessEventOutcomeUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OutcomeApiApplicationTests {

    @Autowired
    private ProcessEventOutcomeUseCase processEventOutcomeUseCase;

    /**
     * Smoke test: the full Spring context wires up without errors, and the core use case bean
     * (the one that talks to Kafka) is actually resolvable.
     */
    @Test
    void contextLoads() {
        assertThat(processEventOutcomeUseCase).isNotNull();
    }

}
