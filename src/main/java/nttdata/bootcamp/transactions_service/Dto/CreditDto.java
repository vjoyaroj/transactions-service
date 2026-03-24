package nttdata.bootcamp.transactions_service.Dto;

import lombok.Data;
import java.time.OffsetDateTime;

/**
 * Credit product projection from credits-service.
 */
@Data
public class CreditDto {
    private String id;
    private String customerId;
    private String creditNumber;
    /** Product type: PERSONAL, BUSINESS, CREDIT_CARD, etc. */
    private String type;
    private Double creditLimit;
    private Double availableBalance;
    private Double amountPaid;
    private String status;
    private Double interestRate;
    private OffsetDateTime createdAt;
}
