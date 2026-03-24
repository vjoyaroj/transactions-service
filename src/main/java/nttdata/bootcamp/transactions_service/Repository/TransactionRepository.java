package nttdata.bootcamp.transactions_service.Repository;

import nttdata.bootcamp.transactions_service.Entity.TransactionDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Reactive MongoDB access for {@link TransactionDocument}.
 */
public interface TransactionRepository extends ReactiveMongoRepository<TransactionDocument, String> {


    /**
     * Counts transactions for a product within a half-open time window.
     *
     * @param productId product identifier
     * @param startInclusive inclusive start instant
     * @param endExclusive exclusive end instant
     * @return number of matching transactions
     */
    @Query(value = "{ 'productId': ?0, 'transactionDate': { $gte: ?1, $lt: ?2 } }", count = true)
    Mono<Long> countByProductIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
            String productId,
            Instant startInclusive,
            Instant endExclusive
    );

    /**
     * All transactions for a product id.
     */
    Flux<TransactionDocument> findByProductId(String productId);

    /**
     * Latest 10 transactions for a product, newest first.
     */
    Flux<TransactionDocument> findTop10ByProductIdOrderByTransactionDateDesc(String productId);
}
