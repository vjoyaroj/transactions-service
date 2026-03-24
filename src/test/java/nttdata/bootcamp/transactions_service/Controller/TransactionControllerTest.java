package nttdata.bootcamp.transactions_service.Controller;

import com.bank.transaction.model.TransactionRequest;
import com.bank.transaction.model.TransactionResponse;
import com.bank.transaction.model.TransferRequest;
import com.bank.transaction.model.TransferResponse;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import nttdata.bootcamp.transactions_service.Service.TransactionService;
import nttdata.bootcamp.transactions_service.Service.TransferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TransactionController} HTTP mapping via {@link TransactionService} and {@link TransferService}.
 */
@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;
    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransactionController controller;

    /** GET /transactions returns 200 with body list. */
    @Test
    void getTransactions_returnsOk() {
        TransactionResponse tr = new TransactionResponse();
        when(transactionService.findAll()).thenReturn(Single.just(List.of(tr)));

        ResponseEntity<List<TransactionResponse>> res = controller.getTransactions().blockingGet();
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
    }

    /** POST /transactions returns 201 Created. */
    @Test
    void createTransaction_returnsCreated() {
        TransactionRequest req = new TransactionRequest();
        TransactionResponse body = new TransactionResponse();
        when(transactionService.createTransaction(req)).thenReturn(Single.just(body));

        ResponseEntity<TransactionResponse> res = controller.createTransaction(req).blockingGet();
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    /** GET by product returns 200 with filtered list. */
    @Test
    void getTransactionsByProduct_returnsOk() {
        TransactionResponse tr = new TransactionResponse();
        when(transactionService.getTransactionsByProduct("p1")).thenReturn(Single.just(List.of(tr)));

        ResponseEntity<List<TransactionResponse>> res =
                controller.getTransactionsByProduct("p1").blockingGet();
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, res.getBody().size());
    }

    /** GET by id returns 200 when found. */
    @Test
    void getTransactionById_returnsOkWhenPresent() {
        TransactionResponse tr = new TransactionResponse();
        when(transactionService.getTransactionById("id")).thenReturn(Maybe.just(tr));

        ResponseEntity<TransactionResponse> res = controller.getTransactionById("id").blockingGet();
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    /** GET by id returns 404 when empty. */
    @Test
    void getTransactionById_returnsNotFoundWhenEmpty() {
        when(transactionService.getTransactionById("x")).thenReturn(Maybe.empty());

        ResponseEntity<TransactionResponse> res = controller.getTransactionById("x").blockingGet();
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }

    /** PUT returns 200. */
    @Test
    void updateTransaction_returnsOk() {
        TransactionRequest req = new TransactionRequest();
        TransactionResponse body = new TransactionResponse();
        when(transactionService.updateTransaction("id", req)).thenReturn(Single.just(body));

        ResponseEntity<TransactionResponse> res = controller.updateTransaction("id", req).blockingGet();
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    /** DELETE returns 204 No Content. */
    @Test
    void deleteTransaction_returnsNoContent() {
        when(transactionService.deleteTransaction("id")).thenReturn(Completable.complete());

        ResponseEntity<Void> res = controller.deleteTransaction("id").blockingGet();
        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
    }

    /** Transfer endpoint returns 201 Created. */
    @Test
    void transferFunds_returnsCreated() {
        TransferRequest treq = new TransferRequest();
        TransferResponse tresp = new TransferResponse();
        when(transferService.transfer(treq)).thenReturn(Single.just(tresp));

        ResponseEntity<TransferResponse> res = controller.transferFunds(treq).blockingGet();
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }
}
