package nttdata.bootcamp.transactions_service.Domain.Policy.Funding;

import com.bank.transaction.model.TransactionRequest;
import lombok.RequiredArgsConstructor;
import nttdata.bootcamp.transactions_service.Client.AccountClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Debits directly from a bank account balance via accounts-service.
 */
@Component
@RequiredArgsConstructor
public class AccountFundingSourcePolicy implements FundingSourcePolicy {
    private final AccountClient accountClient;

    @Override
    public String supportsType() {
        return "ACCOUNT";
    }

    @Override
    public Mono<Void> debit(TransactionRequest request) {
        String accountId = request.getFundingSource().getAccountId();
        if (accountId == null || accountId.isBlank()) {
            return Mono.error(new IllegalArgumentException(
                    "fundingSource.accountId is required when fundingSource.type=ACCOUNT"));
        }
        return accountClient.getAccountById(accountId)
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found")))
                .flatMap(account -> {
                    if (account.getBalance() == null || account.getBalance() < request.getAmount()) {
                        return Mono.error(new IllegalArgumentException(
                                "Insufficient balance in funding account"));
                    }
                    account.setBalance(account.getBalance() - request.getAmount());
                    return accountClient.updateAccount(account.getId(), account).then();
                });
    }
}
