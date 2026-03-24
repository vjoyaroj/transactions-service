package nttdata.bootcamp.transactions_service.Client;

import nttdata.bootcamp.transactions_service.Dto.CreditDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;

/**
 * Reactive HTTP client for credits-service with circuit breaker.
 */
@Component
public class CreditClient {

    private final WebClient webClient;
    private final ReactiveCircuitBreaker circuitBreaker;

    /**
     * @param builder shared WebClient builder
     * @param creditServiceUrl base URL for credits API
     * @param circuitBreakerFactory factory for credit-service breaker
     */
    public CreditClient(WebClient.Builder builder,
                          @Value("${credit.service.url:http://localhost:8083/api/v1}") String creditServiceUrl,
                          ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.webClient = builder.baseUrl(creditServiceUrl).build();
        this.circuitBreaker = circuitBreakerFactory.create("creditServiceCb");
    }

    /**
     * Loads a credit by id.
     *
     * @param id credit id
     * @return credit DTO or error on fallback
     */
    public Mono<CreditDto> getCreditById(String id) {
        return circuitBreaker.run(
            webClient.get()
                .uri("/credits/{id}", id)
                .retrieve()
                .bodyToMono(CreditDto.class),
            throwable -> Mono.error(new RuntimeException("Fallback: Credit Service is currently unavailable or taking too long (Read). Details: " + throwable.getMessage()))
        );
    }

    /**
     * Updates a credit (e.g. available balance after card usage).
     *
     * @param id credit id
     * @param credit payload
     * @return updated credit or error on fallback
     */
    public Mono<CreditDto> updateCredit(String id, CreditDto credit) {
        return circuitBreaker.run(
            webClient.put()
                .uri("/credits/{id}", id)
                .bodyValue(credit)
                .retrieve()
                .bodyToMono(CreditDto.class),
            throwable -> Mono.error(new RuntimeException("Fallback: Credit Service is currently unavailable or taking too long (Update). Details: " + throwable.getMessage()))
        );
    }
}
