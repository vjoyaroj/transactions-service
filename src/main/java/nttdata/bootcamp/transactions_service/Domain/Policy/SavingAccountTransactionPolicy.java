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
 * Movement limits and fees for saving accounts (monthly cap).
 */
@Component
public class SavingAccountTransactionPolicy implements AccountTransactionPolicy {
    private final TransactionRepository repository;

    public SavingAccountTransactionPolicy(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public String supportsAccountType() {
        return "SAVING";
    }

    @Override
    public Mono<Double> validate(AccountDto account, TransactionRequest request) {
        Integer maxMovements = account.getMaxMovements();
        if (maxMovements == null) {
            return Mono.error(new IllegalArgumentException(
                    "Maximum monthly movements reached for SAVING account"));
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
                .flatMap(count -> {
                    if (count >= maxMovements) {
                        return Mono.error(new IllegalArgumentException(
                                "Maximum monthly movements reached for SAVING account"));
                    }
                    // Apply commission if free transaction limit is exceeded
                    Integer freeLimit = account.getFreeTransactionLimit();
                    Double fee = account.getTransactionFee();
                    if (freeLimit != null && fee != null && count >= freeLimit) {
                        return Mono.just(fee);
                    }
                    return Mono.just(0.0);
                });
    }
}
