package nttdata.bootcamp.transactions_service.Domain.Policy.Funding;

import com.bank.transaction.model.TransactionRequest;
import reactor.core.publisher.Mono;

/**
 * Debits an external funding source (account, debit card, cash) before persisting the transaction.
 */
public interface FundingSourcePolicy {
    /**
     * @return supported {@code fundingSource.type} value
     */
    String supportsType();

    /**
     * Performs the debit side-effect for the funding source.
     */
    Mono<Void> debit(TransactionRequest request);
}
