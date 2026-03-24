package nttdata.bootcamp.transactions_service.Domain.Policy;

import com.bank.transaction.model.TransactionRequest;
import nttdata.bootcamp.transactions_service.Dto.AccountDto;
import reactor.core.publisher.Mono;

/**
 * Policy interface for account transaction validation.
 * Returns the fee to apply (0.0 if no commission, positive value if commission applies).
 */
public interface AccountTransactionPolicy {
    String supportsAccountType();

    /**
     * Validates the transaction against account rules.
     * Returns a Mono emitting the commission fee to apply (0.0 means no fee).
     * Error signal means transaction is rejected.
     */
    Mono<Double> validate(AccountDto account, TransactionRequest request);
}
