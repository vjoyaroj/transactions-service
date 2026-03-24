package nttdata.bootcamp.transactions_service.Client;

import nttdata.bootcamp.transactions_service.Dto.AccountDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;

/**
 * Reactive HTTP client for accounts-service (get/update) with circuit breaker.
 */
@Component
public class AccountClient {

    private final WebClient webClient;
    private final ReactiveCircuitBreaker circuitBreaker;

    /**
     * @param builder shared WebClient builder
     * @param accountServiceUrl base URL for accounts API
     * @param circuitBreakerFactory factory for account-service breaker
     */
    public AccountClient(WebClient.Builder builder,
                          @Value("${account.service.url:http://localhost:8082/api/v1}") String accountServiceUrl,
                          ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.webClient = builder.baseUrl(accountServiceUrl).build();
        this.circuitBreaker = circuitBreakerFactory.create("accountServiceCb");
    }

    /**
     * Loads an account by id.
     *
     * @param id account identifier
     * @return account DTO or error on fallback
     */
    public Mono<AccountDto> getAccountById(String id) {
        return circuitBreaker.run(
            webClient.get()
                .uri("/accounts/{id}", id)
                .retrieve()
                .bodyToMono(AccountDto.class),
            throwable -> Mono.error(new RuntimeException(
                    "Fallback: Account Service is currently unavailable or taking too long. Details: "
                            + throwable.getMessage()))
        );
    }

    /**
     * Updates an account (e.g. balance after transfer).
     *
     * @param id account id
     * @param account payload
     * @return updated account or error on fallback
     */
    public Mono<AccountDto> updateAccount(String id, AccountDto account) {
        return circuitBreaker.run(
            webClient.put()
                .uri("/accounts/{id}", id)
                .bodyValue(account)
                .retrieve()
                .bodyToMono(AccountDto.class),
            throwable -> Mono.error(new RuntimeException(
                    "Fallback: Account Service is currently unavailable when updating account. Details: "
                            + throwable.getMessage()))
        );
    }
}
