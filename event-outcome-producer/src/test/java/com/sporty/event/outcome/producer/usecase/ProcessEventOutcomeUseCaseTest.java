package com.sporty.event.outcome.producer.usecase;

import com.sporty.event.outcome.producer.adapter.out.messaging.EventOutcomeMessage;
import com.sporty.event.outcome.producer.adapter.out.messaging.EventOutcomePublisher;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ProcessEventOutcomeUseCaseTest {

    @Mock
    private EventOutcomePublisher eventOutcomePublisher;

    private ProcessEventOutcomeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessEventOutcomeUseCase(eventOutcomePublisher);
    }

    @Test
    void publishesValidEventOutcomeToPublisher() {
        EventOutcomeMessage message = new EventOutcomeMessage(
                UUID.fromString("0db0af77-e859-4fdb-9f99-870a39f1daa0"),
                "Monza Grand Prix",
                UUID.fromString("c969635a-72fd-4e16-9274-b3660cbd5f74")
        );

        useCase.publishEventOutcome(message);

        verify(eventOutcomePublisher).publish(message);
        verifyNoMoreInteractions(eventOutcomePublisher);
    }

    @Test
    void stillForwardsMessageWithNullEventName() {
        EventOutcomeMessage message = new EventOutcomeMessage(
                UUID.fromString("3cb09b73-f663-4df7-af95-65d7635b9752"),
                null,
                UUID.fromString("7d4dbf54-d2c6-4390-9eaf-acd4ed43f0fe")
        );

        useCase.publishEventOutcome(message);

        verify(eventOutcomePublisher).publish(message);
        verifyNoMoreInteractions(eventOutcomePublisher);
    }

    @Test
    void propagatesExceptionWhenPublisherFails() {
        EventOutcomeMessage message = new EventOutcomeMessage(
                UUID.fromString("3bfca4b2-0648-40da-94e5-dd67e6763015"),
                "Singapore Grand Prix",
                UUID.fromString("59ec2634-13c8-4b3d-b62f-0184d75bd6c0")
        );
        TimeoutException exception = new TimeoutException("Kafka timeout");
        doThrow(exception).when(eventOutcomePublisher).publish(message);

        assertThatThrownBy(() -> useCase.publishEventOutcome(message))
                .isSameAs(exception);
    }

}
