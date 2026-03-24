package nttdata.bootcamp.transactions_service.Dto;

import lombok.Data;

import java.util.List;

/**
 * Debit card projection from debit-cards-service.
 */
@Data
public class DebitCardDto {
    private String id;
    private String customerId;
    private String cardNumber;
    private String status;
    private List<DebitCardAccountLinkDto> linkedAccounts;
}
