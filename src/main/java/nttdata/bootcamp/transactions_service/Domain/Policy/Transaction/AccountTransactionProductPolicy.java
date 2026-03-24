package nttdata.bootcamp.transactions_service.Domain.Policy.Transaction;

import com.bank.transaction.model.TransactionRequest;
import lombok.RequiredArgsConstructor;
import nttdata.bootcamp.transactions_service.Client.AccountClient;
import nttdata.bootcamp.transactions_service.Domain.Policy.AccountTransactionPolicyRegistry;
import nttdata.bootcamp.transactions_service.Domain.Policy.Funding.FundingSourcePolicyRegistry;
import nttdata.bootcamp.transactions_service.Dto.AccountDto;
import nttdata.bootcamp.transactions_service.Entity.TransactionDocument;
import nttdata.bootcamp.transactions_service.Mapper.TransactionMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * {@link TransactionProductPolicy} for bank account products: loads account, applies funding debit and account-type rules.
 */
@Component
@RequiredArgsConstructor
public class AccountTransactionProductPolicy implements TransactionProductPolicy {
    private final AccountClient accountClient;
    private final AccountTransactionPolicyRegistry accountPolicyRegistry;
    private final TransactionMapper mapper;
    private final FundingSourcePolicyRegistry fundingSourcePolicyRegistry;

    @Override
    public String supportsProductType() {
        return "ACCOUNT";
    }

    @Override
    public Mono<TransactionDocument> prepareDocument(TransactionRequest request) {
        return accountClient.getAccountById(request.getProductId())
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found")))
                .flatMap(account -> accountPolicyRegistry
                        .resolve(account.getType())
                        .validate(account, request)
                        .flatMap(fee -> executeAccountMovement(account, request, fee)));
    }

    private Mono<TransactionDocument> executeAccountMovement(
            AccountDto account,
            TransactionRequest request,
            double fee) {
        String txType = request.getTransactionType().getValue();
        if ("DEPOSIT".equals(txType)) {
            return prepareDeposit(request, fee);
        }
        if ("WITHDRAWAL".equals(txType)) {
            return prepareWithdrawal(account, request, fee);
        }
        return prepareFeeOnly(account, request, fee);
    }

    private Mono<TransactionDocument> prepareDeposit(TransactionRequest request, double fee) {
        Mono<Void> fund = debitFundingIfPresent(request);
        return fund.then(Mono.defer(() -> accountClient.getAccountById(request.getProductId())
                .switchIfEmpty(Mono.error(new RuntimeException("Account not found")))
                .flatMap(dest -> {
                    double b = dest.getBalance() != null ? dest.getBalance() : 0.0;
                    dest.setBalance(b + amount(request));
                    if (fee > 0.0) {
                        dest.setBalance(dest.getBalance() - fee);
                    }
                    TransactionDocument doc = mapper.mapToDocument(request);
                    doc.setFee(fee);
                    return accountClient.updateAccount(dest.getId(), dest).thenReturn(doc);
                })));
    }

    private Mono<TransactionDocument> prepareWithdrawal(AccountDto account, TransactionRequest request, double fee) {
        double amt = amount(request);
        double need = amt + fee;
        double b = account.getBalance() != null ? account.getBalance() : 0.0;
        if (b < need) {
            return Mono.error(new IllegalArgumentException("Insufficient balance for withdrawal"));
        }
        account.setBalance(b - need);
        TransactionDocument doc = mapper.mapToDocument(request);
        doc.setFee(fee);
        return accountClient.updateAccount(account.getId(), account).thenReturn(doc);
    }

    private Mono<TransactionDocument> prepareFeeOnly(AccountDto account, TransactionRequest request, double fee) {
        TransactionDocument doc = mapper.mapToDocument(request);
        doc.setFee(fee);
        if (fee > 0.0 && account.getBalance() != null) {
            account.setBalance(account.getBalance() - fee);
            return accountClient.updateAccount(account.getId(), account).thenReturn(doc);
        }
        return Mono.just(doc);
    }

    private Mono<Void> debitFundingIfPresent(TransactionRequest request) {
        if (request.getFundingSource() == null || request.getFundingSource().getType() == null) {
            return Mono.empty();
        }
        String type = request.getFundingSource().getType().getValue();
        if ("ACCOUNT".equalsIgnoreCase(type)) {
            String fundingAccountId = request.getFundingSource().getAccountId();
            if (fundingAccountId != null && fundingAccountId.equals(request.getProductId())) {
                return Mono.error(new IllegalArgumentException(
                        "For ACCOUNT-funded deposits, fundingSource.accountId must differ from productId"));
            }
        }
        return fundingSourcePolicyRegistry.resolve(type).debit(request);
    }

    private static double amount(TransactionRequest request) {
        return request.getAmount() != null ? request.getAmount() : 0.0;
    }
}
