package nttdata.bootcamp.transactions_service.Domain.Policy.Funding;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves {@link FundingSourcePolicy} by funding source type.
 */
@Component
public class FundingSourcePolicyRegistry {
    private final Map<String, FundingSourcePolicy> byType;

    /**
     * @param policies all funding policies
     */
    public FundingSourcePolicyRegistry(List<FundingSourcePolicy> policies) {
        this.byType = policies.stream().collect(Collectors.toUnmodifiableMap(
                p -> normalizeType(p.supportsType()),
                Function.identity()
        ));
    }

    /**
     * @param sourceType funding source type from request
     * @return matching policy
     */
    public FundingSourcePolicy resolve(String sourceType) {
        FundingSourcePolicy policy = byType.get(normalizeType(sourceType));
        if (policy == null) {
            throw new IllegalArgumentException("Invalid fundingSource.type");
        }
        return policy;
    }

    private static String normalizeType(String type) {
        return type == null ? "" : type.trim().toUpperCase();
    }
}
