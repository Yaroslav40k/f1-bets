package com.sporty.bet.settler.worker;

import com.sporty.bet.settler.worker.adapter.in.messaging.BetSettlementListener;
import com.sporty.bet.settler.worker.usecase.SettleBetsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration")
class BetSettlerWorkerApplicationTests {

    @Autowired
    private SettleBetsUseCase settleBetsUseCase;

    @Autowired
    private BetSettlementListener betSettlementListener;

    /**
     * Smoke test: the full Spring context wires up without errors, and the RocketMQ listener →
     * settlement use case bean chain is actually resolvable, not just that no exception was thrown.
     */
    @Test
    void contextLoads() {
        assertThat(settleBetsUseCase).isNotNull();
        assertThat(betSettlementListener).isNotNull();
    }

}
