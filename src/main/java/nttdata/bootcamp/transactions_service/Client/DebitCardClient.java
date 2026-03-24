package nttdata.bootcamp.transactions_service.Client;

import nttdata.bootcamp.transactions_service.Dto.DebitCardDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Reactive HTTP client for debit-cards-service with circuit breaker.
 */
@Component
public class DebitCardClient {

    private final WebClient webClient;
    private final ReactiveCircuitBreaker circuitBreaker;

    /**
     * @param builder shared WebClient builder
     * @param debitCardServiceUrl base URL for debit card API
     * @param circuitBreakerFactory factory for debit-card breaker
     */
    public DebitCardClient(WebClient.Builder builder,
                           @Value("${debitcard.service.url:http://localhost:8085/api/v1}") String debitCardServiceUrl,
                           ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.webClient = builder.baseUrl(debitCardServiceUrl).build();
        this.circuitBreaker = circuitBreakerFactory.create("debitCardServiceCb");
    }

    /**
     * Loads a debit card by id.
     *
     * @param id debit card id
     * @return card DTO or error on fallback
     */
    public Mono<DebitCardDto> getDebitCardById(String id) {
        return circuitBreaker.run(
                webClient.get()
                        .uri("/debit-cards/{id}", id)
                        .retrieve()
                        .bodyToMono(DebitCardDto.class),
                throwable -> Mono.error(new RuntimeException(
                        "Fallback: Debit Card Service is currently unavailable. Details: " + throwable.getMessage()))
        );
    }
}
