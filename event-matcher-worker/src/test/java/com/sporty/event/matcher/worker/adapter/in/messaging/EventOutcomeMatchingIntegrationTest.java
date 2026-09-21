package com.sporty.event.matcher.worker.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sporty.event.matcher.worker.application.BetManager;
import com.sporty.event.matcher.worker.application.bet.internal.model.Bet;
import com.sporty.event.matcher.worker.application.bet.internal.model.BetStatus;
import com.sporty.event.matcher.worker.application.outbox.OutboxEventManager;
import com.sporty.event.matcher.worker.application.outbox.model.OutboxPayloadType;
import com.sporty.event.matcher.worker.usecase.outbox.DispatchDomainEventsUseCase;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.Message;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.task.scheduling.enabled=false"
})
class EventOutcomeMatchingIntegrationTest {

    @MockitoBean
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private BetManager betManager;

    @Autowired
    private EventOutcomeListener eventOutcomeListener;

    @Autowired
    private DispatchDomainEventsUseCase dispatchDomainEventsUseCase;

    @Autowired
    private OutboxEventManager outboxEventManager;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        var sendResult = mock(SendResult.class);
        when(sendResult.getSendStatus()).thenReturn(SendStatus.SEND_OK);
        when(sendResult.getMsgId()).thenReturn("msg-id");
        when(rocketMQTemplate.syncSendOrderly(anyString(), any(Message.class), anyString())).thenReturn(sendResult);
    }

    @Test
    void onMessage_whenMatchingBetExists_claimsBetAppendsOutboxAndPublishesSettlementWorkUnit() throws Exception {
        var eventId = UUID.randomUUID();
        var eventWinnerId = UUID.randomUUID();

        betManager.create(new Bet(
                UUID.randomUUID(),
                UUID.randomUUID(),
                eventId,
                UUID.randomUUID(),
                eventWinnerId,
                BigDecimal.valueOf(12.50),
                BetStatus.PENDING));

        var pendingBets = betManager.findByEventIdAndStatusOrderByIdAsc(eventId, BetStatus.PENDING, PageRequest.of(0, 10));
        assertThat(pendingBets).singleElement();
        var storedBet = pendingBets.getFirst();

        eventOutcomeListener.onMessage(new EventOutcomeMessage(eventId, "Test Event", eventWinnerId));
        dispatchDomainEventsUseCase.dispatch();

        var dispatchedBets = betManager.findAllById(List.of(storedBet.id()));
        assertThat(dispatchedBets)
                .singleElement()
                .satisfies(dispatchedBet -> assertThat(dispatchedBet.status()).isEqualTo(BetStatus.DISPATCHED));

        var topic = outboxEventManager.getTopicByPayloadType(OutboxPayloadType.BET_SETTLEMENT_WORK_UNIT);
        var messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(rocketMQTemplate).syncSendOrderly(eq(topic), messageCaptor.capture(), anyString());

        var payload = (String) messageCaptor.getValue().getPayload();
        var root = objectMapper.readTree(payload);

        assertThat(root.get("eventId").asText()).isEqualTo(eventId.toString());
        assertThat(root.get("eventWinnerId").asText()).isEqualTo(eventWinnerId.toString());
        assertThat(root.get("bets").size()).isEqualTo(1);
        assertThat(root.get("bets").get(0).get("id").asText()).isEqualTo(storedBet.id().toString());
        assertThat(root.get("bets").get(0).get("status").asText()).isEqualTo(BetStatus.DISPATCHED.name());
    }
}
