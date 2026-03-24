package nttdata.bootcamp.transactions_service.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for a financial movement (deposit, withdrawal, card load, transfer leg, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class TransactionDocument {

    @Id
    private String id;

    private String customerId;

    private String fundingSourceType;

    private String fundingAccountId;

    private String fundingDebitCardId;

    private String productId;

    private String productType;

    private String transactionType;

    private Double amount;

    private Double fee;

    private String description;

    private Instant transactionDate;

}
