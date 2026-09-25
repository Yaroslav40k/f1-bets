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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration")
class BetSettlementListenerIT {

    @Autowired
    BetSettlementListener betSettlementListener;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @MockitoBean
    BetManager betManager;

    @Test
    void onMessage_whenDeliveryIsRedeliveredWithSameKey_processesOnlyOnce() {
        var eventId = UUID.randomUUID();
        var unitWinnerId = UUID.randomUUID();
        var winningBet = bet(eventId, unitWinnerId, new BigDecimal("15.00"));
        var losingBet = bet(eventId, UUID.randomUUID(), new BigDecimal("8.50"));
        var workUnit = new BetWorkUnitMessage(eventId, "Italian Grand Prix", unitWinnerId, List.of(winningBet, losingBet));
        var settlementsCaptor = ArgumentCaptor.forClass(BetSettlement.class);
        var message = message(workUnit);

        betSettlementListener.onMessage(message);

        verify(betManager, times(2)).saveSettlement(settlementsCaptor.capture());
        assertThat(settlementsCaptor.getAllValues()).containsExactly(
                new BetSettlement(winningBet.id(), BetSettlementResult.WON, winningBet.amount()),
                new BetSettlement(losingBet.id(), BetSettlementResult.LOST, losingBet.amount())
        );

        // Same KEYS value models broker redelivery of the same work unit.
        betSettlementListener.onMessage(message);

        verify(betManager, times(2)).saveSettlement(any(BetSettlement.class));
        assertThat(processedMessageCount(message.getKeys())).isOne();
    }

    /**
     * Regression test for the failure mode this design exists to prevent: if the message key were
     * marked as processed before the settlements committed, a failure part-way through would leave
     * the key behind and the broker's redelivery would be silently skipped, losing the settlement.
     * Because the claim shares the use case's transaction, the rollback must remove it again.
     */
    @Test
    void onMessage_whenSettlementFailsMidway_rollsBackTheClaimSoRedeliveryIsReprocessed() {
        var eventId = UUID.randomUUID();
        var unitWinnerId = UUID.randomUUID();
        var firstBet = bet(eventId, unitWinnerId, new BigDecimal("20.00"));
        var secondBet = bet(eventId, UUID.randomUUID(), new BigDecimal("30.00"));
        var workUnit = new BetWorkUnitMessage(eventId, "Belgian Grand Prix", unitWinnerId, List.of(firstBet, secondBet));
        var message = message(workUnit);

        doThrow(new RuntimeException("settlement store unavailable"))
                .when(betManager).saveSettlement(any(BetSettlement.class));

        assertThatThrownBy(() -> betSettlementListener.onMessage(message))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("settlement store unavailable");

        // The claim must not survive the rollback, otherwise the retry below would be skipped.
        assertThat(processedMessageCount(message.getKeys())).isZero();

        doNothing().when(betManager).saveSettlement(any(BetSettlement.class));

        betSettlementListener.onMessage(message);

        assertThat(processedMessageCount(message.getKeys())).isOne();
    }

    private MessageExt message(BetWorkUnitMessage workUnit) {
        var message = new MessageExt();
        message.setKeys(UUID.randomUUID().toString());
        message.setBody(objectMapper.writeValueAsBytes(workUnit));
        return message;
    }

    private Integer processedMessageCount(String messageKey) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM processed_message WHERE message_key = ?", Integer.class, messageKey);
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
