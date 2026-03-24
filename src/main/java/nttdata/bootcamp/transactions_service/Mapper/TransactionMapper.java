package nttdata.bootcamp.transactions_service.Mapper;

import com.bank.transaction.model.FundingSource;
import com.bank.transaction.model.TransactionRequest;
import com.bank.transaction.model.TransactionResponse;
import nttdata.bootcamp.transactions_service.Entity.TransactionDocument;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Maps between API models and {@link TransactionDocument} entities (including funding source and transfer quirks).
 */
@Component
public class TransactionMapper {

    /**
     * Converts a persisted document to the public API response.
     *
     * @param doc stored transaction
     * @return API model
     */
    public TransactionResponse toDTO(TransactionDocument doc) {

        TransactionResponse response = new TransactionResponse();

        response.setId(doc.getId());
        response.setCustomerId(doc.getCustomerId());
        response.setPayerCustomerId(doc.getCustomerId());
        response.setProductId(doc.getProductId());
        response.setProductType(toProductTypeEnum(doc.getProductType()));
        response.setTransactionType(TransactionResponse.TransactionTypeEnum.valueOf(doc.getTransactionType()));
        response.setAmount(doc.getAmount());
        response.setFee(doc.getFee());
        response.setDescription(doc.getDescription());

        if (doc.getTransactionDate() != null) {
            response.setTransactionDate(OffsetDateTime.ofInstant(doc.getTransactionDate(), ZoneOffset.UTC));
        }

        if (doc.getFundingSourceType() != null
                || doc.getFundingAccountId() != null
                || doc.getFundingDebitCardId() != null) {
            FundingSource funding = new FundingSource();
            if (doc.getFundingSourceType() != null) {
                funding.setType(FundingSource.TypeEnum.fromValue(doc.getFundingSourceType()));
            }
            funding.setAccountId(doc.getFundingAccountId());
            funding.setDebitCardId(doc.getFundingDebitCardId());
            response.setFundingSource(funding);
        }

        return response;
    }

    /**
     * Legacy transfer legs stored {@code productType} as {@code TRANSFER}; the API enum only exposes product kinds (e.g. ACCOUNT).
     *
     * @param stored value from persistence
     * @return API product type enum
     */
    private static TransactionResponse.ProductTypeEnum toProductTypeEnum(String stored) {
        if (stored == null) {
            return null;
        }
        if ("TRANSFER".equalsIgnoreCase(stored)) {
            return TransactionResponse.ProductTypeEnum.ACCOUNT;
        }
        return TransactionResponse.ProductTypeEnum.valueOf(stored);
    }

    /**
     * Creates a new document from a create request (new id and current timestamp).
     */
    public TransactionDocument mapToDocument(TransactionRequest request) {

        TransactionDocument doc = new TransactionDocument();

        doc.setId(UUID.randomUUID().toString());
        String payerCustomerId = request.getPayerCustomerId() != null
                ? request.getPayerCustomerId()
                : request.getCustomerId();
        doc.setCustomerId(payerCustomerId);

        if (request.getFundingSource() != null) {
            doc.setFundingSourceType(request.getFundingSource().getType() != null
                    ? request.getFundingSource().getType().getValue()
                    : null);
            doc.setFundingAccountId(request.getFundingSource().getAccountId());
            doc.setFundingDebitCardId(request.getFundingSource().getDebitCardId());
        }
        doc.setProductId(request.getProductId());
        doc.setProductType(request.getProductType().getValue());
        doc.setTransactionType(request.getTransactionType().getValue());
        doc.setAmount(request.getAmount());
        doc.setFee(request.getFee());
        doc.setDescription(request.getDescription());
        doc.setTransactionDate(Instant.now());

        return doc;
    }

    /**
     * Applies update request fields onto an existing document.
     */
    public void updateDocument(TransactionDocument doc, TransactionRequest request) {
        String payerCustomerId = request.getPayerCustomerId() != null
                ? request.getPayerCustomerId()
                : request.getCustomerId();
        doc.setCustomerId(payerCustomerId);

        if (request.getFundingSource() != null) {
            doc.setFundingSourceType(request.getFundingSource().getType() != null
                    ? request.getFundingSource().getType().getValue()
                    : null);
            doc.setFundingAccountId(request.getFundingSource().getAccountId());
            doc.setFundingDebitCardId(request.getFundingSource().getDebitCardId());
        } else {
            doc.setFundingSourceType(null);
            doc.setFundingAccountId(null);
            doc.setFundingDebitCardId(null);
        }
        doc.setProductId(request.getProductId());
        doc.setProductType(request.getProductType().getValue());
        doc.setTransactionType(request.getTransactionType().getValue());
        doc.setAmount(request.getAmount());
        doc.setFee(request.getFee());
        doc.setDescription(request.getDescription());
    }

}
