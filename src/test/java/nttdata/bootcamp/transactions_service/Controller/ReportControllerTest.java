package nttdata.bootcamp.transactions_service.Controller;

import io.reactivex.rxjava3.core.Single;
import nttdata.bootcamp.transactions_service.Service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReportController} PDF report endpoint.
 */
@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController controller;

    /** Report download returns 200 with PDF resource. */
    @Test
    void generateMovementsReport_returnsOkWithPdf() {
        Resource pdf = mock(Resource.class);
        when(reportService.generateMovementsReport("prod-1", "DEBIT")).thenReturn(Single.just(pdf));

        ResponseEntity<Resource> res = controller.generateMovementsReport("prod-1", "DEBIT").blockingGet();

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(pdf, res.getBody());
    }
}
