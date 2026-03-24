package nttdata.bootcamp.transactions_service.Domain.Policy.Funding;

import com.bank.transaction.model.TransactionRequest;
import lombok.RequiredArgsConstructor;
import nttdata.bootcamp.transactions_service.Client.AccountClient;
import nttdata.bootcamp.transactions_service.Client.DebitCardClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Debits via debit card primary linked account chain.
 */
@Component
@RequiredArgsConstructor
public class DebitCardFundingSourcePolicy implements FundingSourcePolicy {
    private final DebitCardClient debitCardClient;
    private final AccountClient accountClient;

    @Override
    public String supportsType() {
        return "DEBIT_CARD";
    }

    @Override
    public Mono<Void> debit(TransactionRequest request) {
        String debitCardId = request.getFundingSource().getDebitCardId();
        if (debitCardId == null || debitCardId.isBlank()) {
            return Mono.error(new IllegalArgumentException(
                    "fundingSource.debitCardId is required when fundingSource.type=DEBIT_CARD"));
        }

        return debitCardClient.getDebitCardById(debitCardId)
                .switchIfEmpty(Mono.error(new RuntimeException("Debit card not found")))
                .flatMap(card -> {
                    if (card.getStatus() != null && !"ACTIVE".equalsIgnoreCase(card.getStatus())) {
                        return Mono.error(new IllegalArgumentException("Debit card is not active"));
                    }
                    if (card.getLinkedAccounts() == null || card.getLinkedAccounts().isEmpty()) {
                        return Mono.error(new IllegalArgumentException("Debit card has no linked accounts"));
                    }
                    String accountId = card.getLinkedAccounts().stream()
                            .filter(link -> Boolean.TRUE.equals(link.getIsPrimary()))
                            .map(link -> link.getAccountId())
                            .findFirst()
                            .orElse(card.getLinkedAccounts().get(0).getAccountId());
                    if (accountId == null || accountId.isBlank()) {
                        return Mono.error(new IllegalArgumentException("Debit card linked account is invalid"));
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
                });
    }
}
