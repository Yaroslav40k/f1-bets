package com.sporty.event.outcome.producer.adapter.in.controller;

import com.sporty.event.outcome.producer.adapter.out.messaging.EventOutcomeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class EventOutcomeControllerIT {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockitoBean
    private KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void publishEventOutcome_validPayload_returnsAcceptedAndSendsToKafka() throws Exception {
        UUID eventId = UUID.fromString("f936be98-a654-4b32-b0c5-2992d0079b8d");
        UUID eventWinnerId = UUID.fromString("c77911a3-a2f7-4a5a-8095-8793c5672c6e");

        mockMvc.perform(post("/api/v1/event-outcome")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "%s",
                                  "eventName": "Italian Grand Prix",
                                  "eventWinnerId": "%s"
                                }
                                """.formatted(eventId, eventWinnerId)))
                .andExpect(status().isAccepted());

        ArgumentCaptor<EventOutcomeMessage> messageCaptor = ArgumentCaptor.forClass(EventOutcomeMessage.class);

        verify(kafkaTemplate).send(eq("event-outcomes"), messageCaptor.capture());
        assertThat(messageCaptor.getValue())
                .extracting(
                        EventOutcomeMessage::eventId,
                        EventOutcomeMessage::eventName,
                        EventOutcomeMessage::eventWinnerId
                )
                .containsExactly(eventId, "Italian Grand Prix", eventWinnerId);
    }

    @Test
    void publishEventOutcome_invalidPayload_returnsClientErrorAndDoesNotSendToKafka() throws Exception {
        mockMvc.perform(post("/api/v1/event-outcome")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "not-a-uuid",
                                  "eventName": "Italian Grand Prix",
                                  "eventWinnerId": "c77911a3-a2f7-4a5a-8095-8793c5672c6e"
                                }
                                """))
                .andExpect(status().is4xxClientError());

        verify(kafkaTemplate, never()).send(eq("event-outcomes"), org.mockito.ArgumentMatchers.any(EventOutcomeMessage.class));
    }

    @Test
    void publishEventOutcome_missingRequiredFields_returnsBadRequestListingEveryOffendingField() throws Exception {
        mockMvc.perform(post("/api/v1/event-outcome")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.eventId").exists())
                .andExpect(jsonPath("$.errors.eventName").exists())
                .andExpect(jsonPath("$.errors.eventWinnerId").exists());

        verify(kafkaTemplate, never()).send(eq("event-outcomes"), org.mockito.ArgumentMatchers.any(EventOutcomeMessage.class));
    }

    @Test
    void publishEventOutcome_blankEventName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/event-outcome")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "f936be98-a654-4b32-b0c5-2992d0079b8d",
                                  "eventName": "   ",
                                  "eventWinnerId": "c77911a3-a2f7-4a5a-8095-8793c5672c6e"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.eventName").exists());

        verify(kafkaTemplate, never()).send(eq("event-outcomes"), org.mockito.ArgumentMatchers.any(EventOutcomeMessage.class));
    }

}
