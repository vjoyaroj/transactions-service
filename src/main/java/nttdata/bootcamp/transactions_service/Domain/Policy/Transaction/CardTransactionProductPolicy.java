package nttdata.bootcamp.transactions_service.Domain.Policy.Transaction;

import com.bank.transaction.model.TransactionRequest;
import lombok.RequiredArgsConstructor;
import nttdata.bootcamp.transactions_service.Client.CreditClient;
import nttdata.bootcamp.transactions_service.Domain.Policy.Funding.FundingSourcePolicyRegistry;
import nttdata.bootcamp.transactions_service.Dto.CreditDto;
import nttdata.bootcamp.transactions_service.Entity.TransactionDocument;
import nttdata.bootcamp.transactions_service.Mapper.TransactionMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * {@link TransactionProductPolicy} for credit card products backed by credits-service.
 */
@Component
@RequiredArgsConstructor
public class CardTransactionProductPolicy implements TransactionProductPolicy {
    private final CreditClient creditClient;
    private final FundingSourcePolicyRegistry fundingSourcePolicyRegistry;
    private final TransactionMapper mapper;

    @Override
    public String supportsProductType() {
        return "CARD";
    }

    @Override
    public Mono<TransactionDocument> prepareDocument(TransactionRequest request) {
        return creditClient.getCreditById(request.getProductId())
                .switchIfEmpty(Mono.error(new RuntimeException("Credit not found")))
                .flatMap(credit -> processCreditByTransactionType(credit, request, true));
    }

    private Mono<TransactionDocument> processCreditByTransactionType(
            CreditDto credit,
            TransactionRequest request,
            boolean cardProduct
    ) {
        if (credit.getStatus() != null && "CLOSED".equalsIgnoreCase(credit.getStatus())) {
            return Mono.error(new IllegalArgumentException("Credit is closed"));
        }

        String txType = request.getTransactionType().getValue();
        if ("CONSUMPTION".equals(txType)) {
            return handleConsumption(credit, request, cardProduct);
        }
        if ("PAYMENT".equals(txType)) {
            return handlePayment(credit, request);
        }
        return Mono.error(new IllegalArgumentException("Invalid transaction type for CREDIT products"));
    }

    private Mono<TransactionDocument> handleConsumption(
            CreditDto credit,
            TransactionRequest request,
            boolean cardProduct
    ) {
        if (!"CREDIT_CARD".equals(credit.getType()) && !cardProduct) {
            return Mono.error(new IllegalArgumentException("Consumptions are only allowed for credit cards"));
        }
        if (credit.getAvailableBalance() != null && credit.getAvailableBalance() < request.getAmount()) {
            return Mono.error(new IllegalArgumentException("Insufficient available balance for consumption"));
        }
        if (credit.getAvailableBalance() != null) {
            credit.setAvailableBalance(credit.getAvailableBalance() - request.getAmount());
        }
        if (credit.getAmountPaid() != null) {
            credit.setAmountPaid(credit.getAmountPaid() + request.getAmount());
        }
        return creditClient.updateCredit(credit.getId(), credit).thenReturn(mapper.mapToDocument(request));
    }

    private Mono<TransactionDocument> handlePayment(CreditDto credit, TransactionRequest request) {
        Mono<Void> debitFunding = Mono.empty();
        if (request.getFundingSource() != null && request.getFundingSource().getType() != null) {
            String sourceType = request.getFundingSource().getType().getValue();
            debitFunding = fundingSourcePolicyRegistry.resolve(sourceType).debit(request);
        }
        if (credit.getAmountPaid() != null) {
            credit.setAmountPaid(Math.max(0, credit.getAmountPaid() - request.getAmount()));
        }
        if (credit.getAvailableBalance() != null && credit.getCreditLimit() != null) {
            credit.setAvailableBalance(Math.min(
                    credit.getCreditLimit(),
                    credit.getAvailableBalance() + request.getAmount()));
        }
        return debitFunding
                .then(creditClient.updateCredit(credit.getId(), credit))
                .thenReturn(mapper.mapToDocument(request));
    }
}
