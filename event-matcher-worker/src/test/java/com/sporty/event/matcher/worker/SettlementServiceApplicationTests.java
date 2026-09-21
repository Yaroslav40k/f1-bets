package com.sporty.event.matcher.worker;

import com.sporty.event.matcher.worker.usecase.bet.DispatchBetsUseCase;
import com.sporty.event.matcher.worker.usecase.bet.MatchEventOutcomeToBetsUseCase;
import com.sporty.event.matcher.worker.usecase.outbox.DispatchDomainEventsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.listener.CommonErrorHandler;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SettlementServiceApplicationTests {

    @Autowired
    private MatchEventOutcomeToBetsUseCase matchEventOutcomeToBetsUseCase;

    @Autowired
    private DispatchBetsUseCase dispatchBetsUseCase;

    @Autowired
    private DispatchDomainEventsUseCase dispatchDomainEventsUseCase;

    @Autowired
    private CommonErrorHandler kafkaErrorHandler;

    /**
     * Smoke test: the full Spring context wires up without errors, and the core Kafka-listener →
     * outbox-dispatch bean chain (plus the Kafka error handler added for poison-record resilience)
     * is actually resolvable, not just that no exception was thrown.
     */
    @Test
    void contextLoads() {
        assertThat(matchEventOutcomeToBetsUseCase).isNotNull();
        assertThat(dispatchBetsUseCase).isNotNull();
        assertThat(dispatchDomainEventsUseCase).isNotNull();
        assertThat(kafkaErrorHandler).isNotNull();
    }

}
