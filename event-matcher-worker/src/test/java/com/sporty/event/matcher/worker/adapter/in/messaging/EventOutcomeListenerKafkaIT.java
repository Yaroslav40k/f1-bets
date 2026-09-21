package com.sporty.event.matcher.worker.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sporty.event.matcher.worker.application.BetManager;
import com.sporty.event.matcher.worker.application.bet.internal.model.Bet;
import com.sporty.event.matcher.worker.application.bet.internal.model.BetStatus;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.Message;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

/**
 * End-to-end test against a real Kafka broker (Testcontainers): a raw producer publishes an
 * {@link EventOutcomeMessage} to the actual {@code event-outcomes} topic and the real
 * {@code @KafkaListener} bean (not a direct method call, unlike {@link EventOutcomeMatchingIntegrationTest})
 * consumes and processes it. This exercises the consumer configuration added for resilience
 * (the {@code ErrorHandlingDeserializer} + bounded-retry {@code DefaultErrorHandler}) in addition
 * to the matching business logic. RocketMQ stays mocked (see class Javadoc on
 * {@link EventOutcomeMatchingIntegrationTest} for the rationale). Skipped automatically when
 * Docker is unavailable.
 */
@SpringBootTest(properties = "spring.task.scheduling.enabled=false")
@Testcontainers(disabledWithoutDocker = true)
class EventOutcomeListenerKafkaIT {

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    // A dedicated, uniquely-named in-memory H2 database avoids leaking outbox rows into the
    // shared "event-matcher-worker" DB other test classes rely on (that DB is process-scoped via
    // DB_CLOSE_DELAY=-1, so it otherwise survives across test classes running in the same JVM fork).
    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.datasource.url",
                () -> "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
    }

    @MockitoBean
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private BetManager betManager;

    @Autowired
    private ObjectMapper objectMapper;

    private org.apache.kafka.clients.producer.Producer<String, String> producer;

    @BeforeEach
    void setUp() {
        var sendResult = mock(SendResult.class);
        when(sendResult.getSendStatus()).thenReturn(SendStatus.SEND_OK);
        when(sendResult.getMsgId()).thenReturn("msg-id");
        when(rocketMQTemplate.syncSendOrderly(anyString(), any(Message.class), anyString())).thenReturn(sendResult);

        Map<String, Object> producerProps = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
        );
        producer = new KafkaProducer<>(producerProps);
    }

    @AfterEach
    void tearDown() {
        producer.close();
    }

    @Test
    void realKafkaMessage_withMatchingPendingBet_isConsumedAndClaimsTheBet() throws Exception {
        var eventId = UUID.randomUUID();
        var eventWinnerId = UUID.randomUUID();

        betManager.create(new Bet(
                UUID.randomUUID(),
                UUID.randomUUID(),
                eventId,
                UUID.randomUUID(),
                eventWinnerId,
                BigDecimal.valueOf(42.0),
                BetStatus.PENDING));

        String json = objectMapper.writeValueAsString(Map.of(
                "eventId", eventId.toString(),
                "eventName", "Dutch Grand Prix",
                "eventWinnerId", eventWinnerId.toString()
        ));
        producer.send(new ProducerRecord<>("event-outcomes", eventId.toString(), json)).get();

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            List<Bet> bets = betManager.findByEventIdAndStatusOrderByIdAsc(eventId, BetStatus.DISPATCHED, PageRequest.of(0, 10));
            assertThat(bets).singleElement();
        });
    }

}
