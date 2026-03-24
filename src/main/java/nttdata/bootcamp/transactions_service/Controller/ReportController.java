package nttdata.bootcamp.transactions_service.Controller;

import com.bank.transaction.api.ReportsApi;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import nttdata.bootcamp.transactions_service.Service.ReportService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for PDF movement reports (OpenAPI {@link ReportsApi}).
 */
@RestController
@RequiredArgsConstructor
public class ReportController implements ReportsApi {

    private final ReportService reportService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Single<ResponseEntity<Resource>> generateMovementsReport(String productId, String cardType) {
        return reportService.generateMovementsReport(productId, cardType)
                .map(pdfResource -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"reporte-movimientos.pdf\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdfResource));
    }
}
