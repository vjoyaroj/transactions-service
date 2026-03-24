package nttdata.bootcamp.transactions_service.Domain.Policy.Transaction;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves {@link TransactionProductPolicy} by normalized product type string.
 */
@Component
public class TransactionProductPolicyRegistry {
    private final Map<String, TransactionProductPolicy> byType;

    /**
     * @param policies all policy beans
     */
    public TransactionProductPolicyRegistry(List<TransactionProductPolicy> policies) {
        this.byType = policies.stream().collect(Collectors.toUnmodifiableMap(
                p -> normalizeType(p.supportsProductType()),
                Function.identity()
        ));
    }

    /**
     * @param productType product type from API
     * @return matching policy
     */
    public TransactionProductPolicy resolve(String productType) {
        TransactionProductPolicy policy = byType.get(normalizeType(productType));
        if (policy == null) {
            throw new IllegalArgumentException("Unknown product type: " + productType);
        }
        return policy;
    }

    private static String normalizeType(String type) {
        return type == null ? "" : type.trim().toUpperCase();
    }
}
