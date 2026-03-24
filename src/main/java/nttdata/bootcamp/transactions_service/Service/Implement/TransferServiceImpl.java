package nttdata.bootcamp.transactions_service.Service.Implement;

import com.bank.transaction.model.TransferRequest;
import com.bank.transaction.model.TransferResponse;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;
import nttdata.bootcamp.transactions_service.Client.AccountClient;
import nttdata.bootcamp.transactions_service.Domain.Policy.Transfer.TransferSourceAccountRule;
import nttdata.bootcamp.transactions_service.Dto.AccountDto;
import nttdata.bootcamp.transactions_service.Entity.TransactionDocument;
import nttdata.bootcamp.transactions_service.Repository.TransactionRepository;
import nttdata.bootcamp.transactions_service.Service.TransferService;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Executes account-to-account transfers: validates source rules, updates balances via accounts-service, persists legs.
 */
@Service
@AllArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final AccountClient accountClient;
    private final TransactionRepository repository;
    private final TransferSourceAccountRule transferSourceAccountRule;
    private final ReactiveStringRedisTemplate redisTemplate;

    /**
     * Evicts cached transaction lists for source/target products after a transfer.
     *
     * @param productId account id used as product key in cache
     * @return completion or empty if null
     */
    private Mono<Void> evictTransactionsByProductCache(String productId) {
        if (productId == null) {
            return Mono.empty();
        }
        String key = "transactionsByProduct:" + productId;
        return redisTemplate.delete(key).then();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Single<TransferResponse> transfer(TransferRequest request) {
        return RxJava3Adapter.monoToSingle(
                accountClient.getAccountById(request.getSourceAccountId())
                        .switchIfEmpty(Mono.error(new RuntimeException("Source account not found")))
                        .flatMap(source -> transferSourceAccountRule.validate(source, request)
                                .flatMap(fee -> accountClient.getAccountById(request.getTargetAccountId())
                                        .switchIfEmpty(Mono.error(new RuntimeException("Target account not found")))
                                        .flatMap(target -> executeTransfer(source, target, request, fee))
                                ))
        );
    }

    /**
     * Applies balance changes, persists WITHDRAWAL/DEPOSIT documents and returns the transfer summary.
     */
    private Mono<TransferResponse> executeTransfer(
            AccountDto source, AccountDto target, TransferRequest request, double sourceFee) {
        applyBalances(source, target, request, sourceFee);

        Instant now = Instant.now();
        TransactionDocument withdrawal = buildDoc(
                request.getCustomerId(), request.getSourceAccountId(),
                "ACCOUNT", "WITHDRAWAL", request.getAmount(), sourceFee, request.getDescription(), now);

        TransactionDocument deposit = buildDoc(
                null, request.getTargetAccountId(),
                "ACCOUNT", "DEPOSIT", request.getAmount(), 0.0, request.getDescription(), now);

        return accountClient.updateAccount(source.getId(), source)
                .flatMap(updatedSource -> accountClient.updateAccount(target.getId(), target)
                        .flatMap(updatedTarget ->
                                repository.save(withdrawal)
                                        .flatMap(savedWithdrawal -> repository.save(deposit)
                                                .map(savedDeposit -> buildTransferResponse(
                                                        request,
                                                        sourceFee,
                                                        now,
                                                        updatedSource,
                                                        updatedTarget,
                                                        savedWithdrawal.getId(),
                                                        savedDeposit.getId()
                                                ))
                                        )
                        )
                )
                .flatMap(resp ->
                        evictTransactionsByProductCache(request.getSourceAccountId())
                                .then(evictTransactionsByProductCache(request.getTargetAccountId()))
                                .thenReturn(resp)
                );
    }

    /**
     * Mutates in-memory balances for source (amount + fee) and target (credit amount).
     */
    private void applyBalances(AccountDto source, AccountDto target, TransferRequest request, double sourceFee) {
        double totalDebit = request.getAmount() + sourceFee;
        source.setBalance(source.getBalance() - totalDebit);
        target.setBalance((target.getBalance() != null ? target.getBalance() : 0.0) + request.getAmount());
    }



    private TransferResponse buildTransferResponse(
            TransferRequest request,
            double sourceFee,
            Instant now,
            AccountDto updatedSource,
            AccountDto updatedTarget,
            String withdrawalTransactionId,
            String depositTransactionId
    ) {
        TransferResponse response = new TransferResponse();
        response.setWithdrawalTransactionId(withdrawalTransactionId);
        response.setDepositTransactionId(depositTransactionId);
        response.setSourceAccountId(request.getSourceAccountId());
        response.setTargetAccountId(request.getTargetAccountId());
        response.setAmount(request.getAmount());
        response.setSourceFee(sourceFee);
        response.setSourceBalanceAfter(updatedSource.getBalance());
        response.setTargetBalanceAfter(updatedTarget.getBalance());
        response.setTransferDate(OffsetDateTime.ofInstant(now, ZoneOffset.UTC));
        return response;
    }


    private TransactionDocument buildDoc(
            String customerId, String productId,
            String productType, String transactionType,
            Double amount, Double fee, String description, Instant date) {
        TransactionDocument doc = new TransactionDocument();
        doc.setId(UUID.randomUUID().toString());
        doc.setCustomerId(customerId);
        doc.setProductId(productId);
        doc.setProductType(productType);
        doc.setTransactionType(transactionType);
        doc.setAmount(amount);
        doc.setFee(fee);
        doc.setDescription(description);
        doc.setTransactionDate(date);
        return doc;
    }
}
