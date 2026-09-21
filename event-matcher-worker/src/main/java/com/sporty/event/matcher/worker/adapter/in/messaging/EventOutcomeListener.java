package com.sporty.event.matcher.worker.adapter.in.messaging;

import com.sporty.event.matcher.worker.usecase.bet.MatchEventOutcomeToBetsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes event outcomes from Kafka and starts the bet-matching workflow.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventOutcomeListener {

    private final MatchEventOutcomeToBetsUseCase matchEventOutcomeToBetsUseCase;


    /**
     * Receives an event outcome from Kafka and hands it to the matching use case.
     *
     * @param dto event outcome message consumed from the {@code event-outcomes} topic.
     */
    @KafkaListener (topics = "${event-matcher-worker.messaging.event.outcome.topic}")
    void onMessage(EventOutcomeMessage dto) {
        log.debug("Received outcome event=[{}], name=[{}]", dto.eventId(), dto.eventName());
        matchEventOutcomeToBetsUseCase.match(dto);
    }
}
