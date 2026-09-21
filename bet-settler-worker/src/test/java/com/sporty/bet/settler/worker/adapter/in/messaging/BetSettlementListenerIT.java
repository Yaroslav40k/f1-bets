package com.sporty.bet.settler.worker.adapter.in.messaging;

import com.sporty.bet.settler.worker.application.bet.BetManager;
import com.sporty.bet.settler.worker.application.bet.internal.model.Bet;
import com.sporty.bet.settler.worker.application.bet.internal.model.BetSettlement;
import com.sporty.bet.settler.worker.application.bet.internal.model.BetSettlementResult;
import com.sporty.bet.settler.worker.application.bet.internal.model.BetStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration")
class BetSettlementListenerIT {

    @Autowired
    BetSettlementListener betSettlementListener;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    BetManager betManager;

    @Test
    void onMessage_whenDeliveryIsRedeliveredWithSameKey_processesOnlyOnce() {
        var eventId = UUID.randomUUID();
        var unitWinnerId = UUID.randomUUID();
        var winningBet = bet(eventId, unitWinnerId, new BigDecimal("15.00"));
        var losingBet = bet(eventId, UUID.randomUUID(), new BigDecimal("8.50"));
        var workUnit = new BetWorkUnitMessage(eventId, "Italian Grand Prix", unitWinnerId, List.of(winningBet, losingBet));
        var eventKey = UUID.randomUUID().toString();
        var settlementsCaptor = ArgumentCaptor.forClass(BetSettlement.class);
        var message = new MessageExt();
        message.setKeys(eventKey);
        message.setBody(objectMapper.writeValueAsBytes(workUnit));

        betSettlementListener.onMessage(message);

        verify(betManager, times(2)).saveSettlement(settlementsCaptor.capture());
        assertThat(settlementsCaptor.getAllValues()).containsExactly(
                new BetSettlement(winningBet.id(), BetSettlementResult.WON, winningBet.amount()),
                new BetSettlement(losingBet.id(), BetSettlementResult.LOST, losingBet.amount())
        );

        // Same KEYS value models broker redelivery of the same work unit.
        betSettlementListener.onMessage(message);

        verify(betManager, times(2)).saveSettlement(org.mockito.ArgumentMatchers.any(BetSettlement.class));
    }

    private static Bet bet(UUID eventId, UUID betWinnerId, BigDecimal amount) {
        return new Bet(
                UUID.randomUUID(),
                UUID.randomUUID(),
                eventId,
                UUID.randomUUID(),
                betWinnerId,
                amount,
                BetStatus.DISPATCHED
        );
    }
}
