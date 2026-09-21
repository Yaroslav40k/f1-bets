package com.sporty.event.outcome.producer.adapter.in.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * End-to-end test against a real Kafka broker (Testcontainers), unlike {@link EventOutcomeControllerIT}
 * which mocks {@code KafkaTemplate}. This proves the producer configuration (serializer, topic
 * property) actually works against a real broker, not just that the bean is invoked correctly.
 * Skipped automatically when Docker is unavailable.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class EventOutcomeControllerKafkaIT {

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private KafkaConsumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        );
        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(java.util.List.of("event-outcomes"));
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void publishEventOutcome_validPayload_isReadableFromRealKafkaTopic() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID eventWinnerId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/event-outcome")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "%s",
                                  "eventName": "Belgian Grand Prix",
                                  "eventWinnerId": "%s"
                                }
                                """.formatted(eventId, eventWinnerId)))
                .andExpect(status().isAccepted());

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            assertThat(records)
                    .anySatisfy(record -> assertThat(record.value())
                            .contains(eventId.toString())
                            .contains(eventWinnerId.toString())
                            .contains("Belgian Grand Prix"));
        });
    }

}
