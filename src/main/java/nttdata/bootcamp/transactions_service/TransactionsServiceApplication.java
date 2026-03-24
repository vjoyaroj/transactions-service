package nttdata.bootcamp.transactions_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Bootstrap for the Transactions microservice (REST API and Kafka consumers).
 */
@SpringBootApplication
@EnableKafka
public class TransactionsServiceApplication {

	/**
	 * @param args standard Spring Boot arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(TransactionsServiceApplication.class, args);
	}

}
