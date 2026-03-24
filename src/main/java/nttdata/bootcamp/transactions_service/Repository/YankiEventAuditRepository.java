package nttdata.bootcamp.transactions_service.Repository;

import nttdata.bootcamp.transactions_service.Entity.YankiEventAuditDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Idempotent audit store for consumed Yanki Kafka events.
 */
public interface YankiEventAuditRepository extends ReactiveMongoRepository<YankiEventAuditDocument, String> {
    /**
     * @param eventId business event id from payload
     * @return whether an audit row already exists
     */
    Mono<Boolean> existsByEventId(String eventId);
}
