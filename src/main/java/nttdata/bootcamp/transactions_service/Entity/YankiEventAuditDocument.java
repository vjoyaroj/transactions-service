package nttdata.bootcamp.transactions_service.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB audit row for a consumed Yanki domain event (idempotent by {@link #eventId}).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "yanki_event_audit")
public class YankiEventAuditDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String eventId;
    private String eventType;
    private String topic;
    private String payload;
    private Instant receivedAt;
}
