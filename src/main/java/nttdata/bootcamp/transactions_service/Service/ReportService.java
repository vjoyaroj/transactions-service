package nttdata.bootcamp.transactions_service.Service;

import io.reactivex.rxjava3.core.Single;
import org.springframework.core.io.Resource;

/**
 * Service for generating PDF reports.
 */
public interface ReportService {

    /**
     * Generates a report of the last 10 movements for a debit or credit card product.
     *
     * @param productId  ID of the product (account or credit)
     * @param cardType   optional label: DEBIT or CREDIT (used only for report header)
     * @return PDF as {@link Resource}
     */
    Single<Resource> generateMovementsReport(String productId, String cardType);
}
