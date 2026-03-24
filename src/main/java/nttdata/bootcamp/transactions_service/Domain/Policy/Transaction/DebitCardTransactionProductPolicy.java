package nttdata.bootcamp.transactions_service.Domain.Policy.Transaction;

import com.bank.transaction.model.TransactionRequest;
import lombok.RequiredArgsConstructor;
import nttdata.bootcamp.transactions_service.Entity.TransactionDocument;
import nttdata.bootcamp.transactions_service.Mapper.TransactionMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * {@link TransactionProductPolicy} for debit card products (simple mapping).
 */
@Component
@RequiredArgsConstructor
public class DebitCardTransactionProductPolicy implements TransactionProductPolicy {
    private final TransactionMapper mapper;

    @Override
    public String supportsProductType() {
        return "DEBIT_CARD";
    }

    @Override
    public Mono<TransactionDocument> prepareDocument(TransactionRequest request) {
        return Mono.just(mapper.mapToDocument(request));
    }
}
