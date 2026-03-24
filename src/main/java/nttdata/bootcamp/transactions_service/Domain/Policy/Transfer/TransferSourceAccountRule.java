package nttdata.bootcamp.transactions_service.Domain.Policy.Transfer;

import com.bank.transaction.model.TransactionRequest;
import com.bank.transaction.model.TransferRequest;
import lombok.RequiredArgsConstructor;
import nttdata.bootcamp.transactions_service.Domain.Policy.AccountTransactionPolicyRegistry;
import nttdata.bootcamp.transactions_service.Dto.AccountDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Validates the source account for transfers (active, balance, commissions via account policies).
 */
@Component
@RequiredArgsConstructor
public class TransferSourceAccountRule {
    private final AccountTransactionPolicyRegistry accountPolicyRegistry;

    /**
     * Ensures the source account can cover the transfer and returns the fee to charge on the withdrawal leg.
     *
     * @param source source account from accounts-service
     * @param request transfer request
     * @return fee amount for the source leg
     */
    public Mono<Double> validate(AccountDto source, TransferRequest request) {
        if (!"ACTIVE".equals(source.getStatus())) {
            return Mono.error(new IllegalArgumentException(
                    "Source account is not active. Status: " + source.getStatus()));
        }
        if (source.getBalance() == null || source.getBalance() < request.getAmount()) {
            return Mono.error(new IllegalArgumentException(
                    "Insufficient balance in source account. Available: "
                            + (source.getBalance() != null ? source.getBalance() : 0.0)
                            + ", Required: " + request.getAmount()));
        }

        TransactionRequest txForPolicy = new TransactionRequest(
                request.getCustomerId(),
                request.getSourceAccountId(),
                TransactionRequest.ProductTypeEnum.ACCOUNT,
                TransactionRequest.TransactionTypeEnum.TRANSFER,
                request.getAmount()
        );
        return accountPolicyRegistry.resolve(source.getType()).validate(source, txForPolicy);
    }
}
