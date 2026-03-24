package nttdata.bootcamp.transactions_service.Dto;

import lombok.Data;

/**
 * Linked account entry on a debit card.
 */
@Data
public class DebitCardAccountLinkDto {
    private String accountId;
    private Boolean isPrimary;
}
