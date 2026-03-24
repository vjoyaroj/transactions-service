package nttdata.bootcamp.transactions_service.Events;

import com.fasterxml.jackson.databind.ObjectMapper;
import nttdata.bootcamp.transactions_service.Entity.YankiEventAuditDocument;
import nttdata.bootcamp.transactions_service.Repository.YankiEventAuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link YankiEventConsumer#process}: idempotent audit persistence.
 */
@ExtendWith(MockitoExtension.class)
class YankiEventConsumerTest {

    @Mock
    private YankiEventAuditRepository auditRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** New eventId triggers save. */
    @Test
    void process_persistsNewEvent() {
        YankiEventConsumer consumer = new YankiEventConsumer(auditRepository, objectMapper);
        String payload = "{\"eventId\":\"evt-1\",\"eventType\":\"WALLET_CREATED\"}";
        when(auditRepository.existsByEventId("evt-1")).thenReturn(Mono.just(false));
        when(auditRepository.save(any(YankiEventAuditDocument.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        consumer.process(payload, "bank.yanki.wallet.v1").block();

        ArgumentCaptor<YankiEventAuditDocument> cap = ArgumentCaptor.forClass(YankiEventAuditDocument.class);
        verify(auditRepository).save(cap.capture());
        assertEquals("evt-1", cap.getValue().getEventId());
        assertEquals("WALLET_CREATED", cap.getValue().getEventType());
    }

    /** Missing event fields skip repository. */
    @Test
    void process_skipsWhenPayloadIncomplete() {
        YankiEventConsumer consumer = new YankiEventConsumer(auditRepository, objectMapper);

        consumer.process("{}", "t").block();

        verifyNoInteractions(auditRepository);
    }
}
