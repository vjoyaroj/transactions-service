package nttdata.bootcamp.transactions_service.Domain.Policy;

import com.bank.transaction.model.TransactionRequest;
import nttdata.bootcamp.transactions_service.Dto.AccountDto;
import nttdata.bootcamp.transactions_service.Repository.TransactionRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * Commission rules for current accounts (free tx limit and per-tx fee).
 */
@Component
public class CurrentAccountTransactionPolicy implements AccountTransactionPolicy {
    private final TransactionRepository repository;

    public CurrentAccountTransactionPolicy(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public String supportsAccountType() {
        return "CURRENT";
    }

    @Override
    public Mono<Double> validate(AccountDto account, TransactionRequest request) {
        Integer freeLimit = account.getFreeTransactionLimit();
        Double fee = account.getTransactionFee();

        // No commission configured: allow freely
        if (freeLimit == null || fee == null) {
            return Mono.just(0.0);
        }

        YearMonth currentMonth = YearMonth.now();
        ZoneId zone = ZoneId.systemDefault();
        Instant start = currentMonth.atDay(1).atStartOfDay(zone).toInstant();
        Instant endExclusive = currentMonth.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant();

        return repository.countByProductIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                        request.getProductId(),
                        start,
                        endExclusive
                )
                .map(count -> count >= freeLimit ? fee : 0.0);
    }
}
