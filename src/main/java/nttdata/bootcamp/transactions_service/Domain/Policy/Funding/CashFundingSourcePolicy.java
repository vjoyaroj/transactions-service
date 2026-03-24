package nttdata.bootcamp.transactions_service.Domain.Policy.Funding;

import com.bank.transaction.model.TransactionRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * No-op funding for cash-originated movements (no remote debit).
 */
@Component
public class CashFundingSourcePolicy implements FundingSourcePolicy {
    @Override
    public String supportsType() {
        return "CASH";
    }

    @Override
    public Mono<Void> debit(TransactionRequest request) {
        return Mono.empty();
    }
}
