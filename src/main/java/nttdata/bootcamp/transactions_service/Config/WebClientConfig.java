package nttdata.bootcamp.transactions_service.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Shared {@link WebClient.Builder} for outbound reactive HTTP clients.
 */
@Configuration
public class WebClientConfig {

    /**
     * @return reusable WebClient builder bean
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
