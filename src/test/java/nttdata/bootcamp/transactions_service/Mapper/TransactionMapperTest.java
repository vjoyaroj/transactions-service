package nttdata.bootcamp.transactions_service.Mapper;

import com.bank.transaction.model.FundingSource;
import com.bank.transaction.model.TransactionRequest;
import com.bank.transaction.model.TransactionResponse;
import nttdata.bootcamp.transactions_service.Entity.TransactionDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link TransactionMapper} round-trips and legacy TRANSFER product type mapping.
 */
class TransactionMapperTest {

    private final TransactionMapper mapper = new TransactionMapper();

    /** Stored TRANSFER maps to ACCOUNT in API enum. */
    @Test
    void toDTO_mapsTransferProductTypeToAccount() {
        TransactionDocument doc = TransactionDocument.builder()
                .id("t1")
                .customerId("c1")
                .productId("p1")
                .productType("TRANSFER")
                .transactionType("DEPOSIT")
                .amount(1d)
                .fee(0d)
                .description("d")
                .transactionDate(Instant.parse("2024-01-01T00:00:00Z"))
                .build();

        TransactionResponse r = mapper.toDTO(doc);
        assertEquals(TransactionResponse.ProductTypeEnum.ACCOUNT, r.getProductType());
    }

    /** Funding fields on document appear in API fundingSource. */
    @Test
    void toDTO_includesFundingSourceWhenPresent() {
        TransactionDocument doc = TransactionDocument.builder()
                .id("t2")
                .customerId("c1")
                .productId("p1")
                .productType("ACCOUNT")
                .transactionType("PAYMENT")
                .amount(5d)
                .fundingSourceType("ACCOUNT")
                .fundingAccountId("acc-1")
                .build();

        TransactionResponse r = mapper.toDTO(doc);
        assertEquals(FundingSource.TypeEnum.ACCOUNT, r.getFundingSource().getType());
        assertEquals("acc-1", r.getFundingSource().getAccountId());
    }

    /** Payer id from request becomes customerId on document. */
    @Test
    void mapToDocument_usesPayerCustomerId() {
        TransactionRequest req = new TransactionRequest("p1", "prod", TransactionRequest.ProductTypeEnum.ACCOUNT,
                TransactionRequest.TransactionTypeEnum.DEPOSIT, 10d);
        req.setFee(1d);
        req.setDescription("x");

        TransactionDocument doc = mapper.mapToDocument(req);
        assertEquals("p1", doc.getCustomerId());
        assertEquals("ACCOUNT", doc.getProductType());
        assertNotNull(doc.getTransactionDate());
    }

    /** Legacy customerId is used when payer id absent. */
    @Test
    void mapToDocument_fallsBackToDeprecatedCustomerId() {
        TransactionRequest req = new TransactionRequest();
        req.setCustomerId("legacy-c");
        req.setProductId("prod");
        req.setProductType(TransactionRequest.ProductTypeEnum.ACCOUNT);
        req.setTransactionType(TransactionRequest.TransactionTypeEnum.DEPOSIT);
        req.setAmount(1d);

        TransactionDocument doc = mapper.mapToDocument(req);
        assertEquals("legacy-c", doc.getCustomerId());
    }

    /** Null funding on update clears stored funding columns. */
    @Test
    void updateDocument_clearsFundingWhenNull() {
        TransactionDocument doc = new TransactionDocument();
        doc.setFundingAccountId("old");
        TransactionRequest req = new TransactionRequest("p1", "prod", TransactionRequest.ProductTypeEnum.CREDIT,
                TransactionRequest.TransactionTypeEnum.PAYMENT, 2d);
        req.setFundingSource(null);

        mapper.updateDocument(doc, req);
        assertNull(doc.getFundingAccountId());
    }

    /** Update merges funding source fields. */
    @Test
    void updateDocument_appliesFundingSource() {
        TransactionDocument doc = new TransactionDocument();
        TransactionRequest req = new TransactionRequest("p1", "prod", TransactionRequest.ProductTypeEnum.DEBIT_CARD,
                TransactionRequest.TransactionTypeEnum.CONSUMPTION, 3d);
        FundingSource fs = new FundingSource();
        fs.setType(FundingSource.TypeEnum.DEBIT_CARD);
        fs.setDebitCardId("dc-9");
        req.setFundingSource(fs);

        mapper.updateDocument(doc, req);
        assertEquals("DEBIT_CARD", doc.getFundingSourceType());
        assertEquals("dc-9", doc.getFundingDebitCardId());
    }

    /** Null transaction date stays null on response. */
    @Test
    void toDTO_withNullTransactionDate() {
        TransactionDocument doc = TransactionDocument.builder()
                .id("t3")
                .customerId("c")
                .productId("p")
                .productType("ACCOUNT")
                .transactionType("WITHDRAWAL")
                .amount(1d)
                .transactionDate(null)
                .build();

        TransactionResponse r = mapper.toDTO(doc);
        assertNull(r.getTransactionDate());
    }
}
