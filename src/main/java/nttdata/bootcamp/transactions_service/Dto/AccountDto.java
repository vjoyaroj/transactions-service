package nttdata.bootcamp.transactions_service.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Account projection used when calling accounts-service (read/update balance).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private String id;
    private String customerId;
    private String accountNumber;
    /** Account kind: SAVING, CURRENT, FIXED_TERM. */
    private String type;
    private Double balance;
    private Double maintenanceFee;
    private Integer maxMovements;
    private Integer freeTransactionLimit;
    private Double transactionFee;
    private Integer specificDay;
    private String status;
    /** Required when calling accounts-service PUT {@code /accounts/{id}}. */
    private List<String> authorizedSignatories;
}
