package com.sporty.event.outcome.producer.adapter.in.controller;

import com.sporty.event.outcome.producer.adapter.out.messaging.EventOutcomeMessage;
import com.sporty.event.outcome.producer.usecase.ProcessEventOutcomeUseCase;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the HTTP endpoint used to publish sports event outcomes into the platform.
 */
@RestController
@RequestMapping("/api/v1/event-outcome")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventOutcomeController {

    ProcessEventOutcomeUseCase processEventOutcomeUseCase;

    /**
     * Accepts an event outcome and hands it off for asynchronous Kafka publication.
     *
     * @param request event outcome submitted by the API caller.
     * @return {@code 202 Accepted} once the message has been queued for publication.
     */
    @PostMapping
    public ResponseEntity<Void> publishEvent(@Valid @RequestBody PublishEventOutcomeRequest request) {
        var message = new EventOutcomeMessage(
                request.eventId(), request.eventName(), request.eventWinnerId());
        processEventOutcomeUseCase.publishEventOutcome(message);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
