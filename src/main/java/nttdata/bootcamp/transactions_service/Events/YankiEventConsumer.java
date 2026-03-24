package nttdata.bootcamp.transactions_service.Events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nttdata.bootcamp.transactions_service.Entity.YankiEventAuditDocument;
import nttdata.bootcamp.transactions_service.Repository.YankiEventAuditRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Consumes Yanki wallet/payment events from Kafka and idempotently stores them for audit.
 */
@Component
@RequiredArgsConstructor
public class YankiEventConsumer {
    private final YankiEventAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    /**
     * Listens to wallet topic and persists unknown events by {@code eventId}.
     */
    @KafkaListener(topics = "${yanki.kafka.topics.wallet:bank.yanki.wallet.v1}")
    public void consumeWalletEvent(String payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        process(payload, topic).subscribe();
    }

    /**
     * Listens to payment topic and persists unknown events by {@code eventId}.
     */
    @KafkaListener(topics = "${yanki.kafka.topics.payment:bank.yanki.payment.v1}")
    public void consumePaymentEvent(String payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        process(payload, topic).subscribe();
    }

    /**
     * Parses JSON payload and inserts audit row when {@code eventId} is new.
     *
     * @param payload raw JSON string
     * @param topic Kafka topic name
     * @return empty completion on skip or success
     */
    Mono<Void> process(String payload, String topic) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String eventId = node.path("eventId").asText(null);
            String eventType = node.path("eventType").asText(null);
            if (eventId == null || eventType == null) {
                return Mono.empty();
            }
            return auditRepository.existsByEventId(eventId)
                    .flatMap(exists -> exists ? Mono.empty() : auditRepository.save(
                            YankiEventAuditDocument.builder()
                                    .id(UUID.randomUUID().toString())
                                    .eventId(eventId)
                                    .eventType(eventType)
                                    .topic(topic)
                                    .payload(payload)
                                    .receivedAt(Instant.now())
                                    .build()).then());
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}
