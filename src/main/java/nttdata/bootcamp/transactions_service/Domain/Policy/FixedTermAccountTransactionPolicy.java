package nttdata.bootcamp.transactions_service.Domain.Policy;

import com.bank.transaction.model.TransactionRequest;
import nttdata.bootcamp.transactions_service.Dto.AccountDto;
import nttdata.bootcamp.transactions_service.Repository.TransactionRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * Commission and movement rules for fixed-term accounts (specific day window).
 */
@Component
public class FixedTermAccountTransactionPolicy implements AccountTransactionPolicy {
    private final TransactionRepository repository;

    public FixedTermAccountTransactionPolicy(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public String supportsAccountType() {
        return "FIXED_TERM";
    }

    @Override
    public Mono<Double> validate(AccountDto account, TransactionRequest request) {
        int today = LocalDate.now().getDayOfMonth();
        if (account.getSpecificDay() != null && account.getSpecificDay() != today) {
            return Mono.error(new IllegalArgumentException(
                    "Transactions for FIXED_TERM account are only allowed on day "
                            + account.getSpecificDay() + " of the month"));
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
                    if (count >= 1) {
                        return Mono.error(new IllegalArgumentException(
                                "FIXED_TERM account allows only 1 movement per month"));
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
