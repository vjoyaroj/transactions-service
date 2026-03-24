package nttdata.bootcamp.transactions_service.Domain.Policy.Transaction;

import com.bank.transaction.model.TransactionRequest;
import nttdata.bootcamp.transactions_service.Entity.TransactionDocument;
import reactor.core.publisher.Mono;

/**
 * Prepares a {@link TransactionDocument} for a given API product type (account, card, credit, …).
 */
public interface TransactionProductPolicy {
    /**
     * @return supported {@link TransactionRequest} product type value
     */
    String supportsProductType();

    /**
     * Validates external dependencies, applies fees/debits and returns a document ready to save.
     */
    Mono<TransactionDocument> prepareDocument(TransactionRequest request);
}
