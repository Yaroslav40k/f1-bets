package com.sporty.bet.settler.worker.adapter.in.messaging;

import com.sporty.bet.settler.worker.application.idempotency.DuplicateMessageException;
import com.sporty.bet.settler.worker.usecase.SettleBetsCommand;
import com.sporty.bet.settler.worker.usecase.SettleBetsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Consumes raw {@code MessageExt} records from RocketMQ so the {@code KEYS} header can be reused
 * as the idempotency key. The listener keeps the native message instead of a converted DTO because
 * the partition key only affects ordering, while {@code KEYS} is the stable message identity.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "${bet-settler-worker.rocketmq.topic}",
        consumerGroup = "${bet-settler-worker.rocketmq.consumer-group}"
)
public class BetSettlementListener implements RocketMQListener<MessageExt> {

    private final SettleBetsUseCase settleBetsUseCase;
    private final ObjectMapper objectMapper;
    private final SettleBetsCommandMapper betsCommandMapper;

    /**
     * Deserializes a settlement work unit and forwards it for idempotent processing.
     *
     * <p>A message that fails to deserialize is a poison record that will never parse
     * successfully no matter how many times RocketMQ redelivers it, so the failure is logged
     * and the message is dropped here rather than left to exhaust RocketMQ's own reconsume
     * attempts before landing on the dead-letter topic.
     *
     * <p>A duplicate delivery is acknowledged rather than retried. The exception is caught here,
     * outside the use case's transaction boundary, on purpose: swallowing it inside the
     * transaction would leave that transaction marked rollback-only and the commit would then
     * fail with {@code UnexpectedRollbackException}, turning every duplicate into an endless
     * redelivery loop.
     *
     * @param message raw RocketMQ message carrying the serialized {@code BetWorkUnitMessage}.
     */
    @Override
    public void onMessage(MessageExt message) {
        var eventKey = message.getKeys();
        BetWorkUnitMessage unit;
        try {
            unit = objectMapper.readValue(message.getBody(), BetWorkUnitMessage.class);
        } catch (JacksonException e) {
            log.error("Discarding unparseable settlement message key=[{}]", eventKey, e);
            return;
        }
        log.debug("Received settlement unit key=[{}], event=[{}], bets=[{}]",
                eventKey, unit.eventId(), unit.bets().size());
        try {
            var command = betsCommandMapper.toCommand(unit);
            settleBetsUseCase.settleBets(eventKey, command);
        } catch (DuplicateMessageException e) {
            log.info("Skipping duplicate delivery of settlement unit [{}] for event [{}]",
                    eventKey, unit.eventId());
        }
    }
}
