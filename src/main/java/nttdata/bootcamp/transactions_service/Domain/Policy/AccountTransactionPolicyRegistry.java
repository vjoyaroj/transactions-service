package nttdata.bootcamp.transactions_service.Domain.Policy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves {@link AccountTransactionPolicy} by account type (SAVING, CURRENT, …).
 */
@Component
public class AccountTransactionPolicyRegistry {
    private final Map<String, AccountTransactionPolicy> byType;

    /**
     * @param policies all account-type policies
     */
    public AccountTransactionPolicyRegistry(List<AccountTransactionPolicy> policies) {
        this.byType = policies.stream()
                .collect(Collectors.toUnmodifiableMap(
                        p -> normalizeType(p.supportsAccountType()),
                        Function.identity()
                ));
    }

    /**
     * @param accountType account type string from {@link nttdata.bootcamp.transactions_service.Dto.AccountDto}
     * @return matching policy
     */
    public AccountTransactionPolicy resolve(String accountType) {
        AccountTransactionPolicy policy = byType.get(normalizeType(accountType));
        if (policy == null) {
            throw new IllegalArgumentException("Unknown account type: " + accountType);
        }
        return policy;
    }

    private static String normalizeType(String type) {
        return type == null ? "" : type.trim().toUpperCase();
    }
}

