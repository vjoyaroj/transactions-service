package nttdata.bootcamp.transactions_service.Controller;

import com.bank.transaction.api.TransactionsApi;
import com.bank.transaction.model.TransactionRequest;
import com.bank.transaction.model.TransactionResponse;
import com.bank.transaction.model.TransferRequest;
import com.bank.transaction.model.TransferResponse;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import nttdata.bootcamp.transactions_service.Service.TransactionService;
import nttdata.bootcamp.transactions_service.Service.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Reactive REST controller for transactions and internal transfers (OpenAPI implementation).
 */
@RestController
@RequiredArgsConstructor
public class TransactionController implements TransactionsApi {

    private final TransactionService transactionService;
    private final TransferService transferService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Single<ResponseEntity<List<TransactionResponse>>> getTransactions() {
        return transactionService.findAll()
                .map(ResponseEntity::ok);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Single<ResponseEntity<List<TransactionResponse>>> getTransactionsByProduct(String productId) {
        return transactionService.getTransactionsByProduct(productId)
                .map(ResponseEntity::ok);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Single<ResponseEntity<TransactionResponse>> createTransaction(TransactionRequest transactionRequest) {
        return transactionService.createTransaction(transactionRequest)
                .map(transaction -> ResponseEntity.status(HttpStatus.CREATED).body(transaction));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Single<ResponseEntity<TransactionResponse>> getTransactionById(String id) {
        return transactionService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Single<ResponseEntity<TransactionResponse>> updateTransaction(String id,
                                                                         TransactionRequest transactionRequest) {
        return transactionService.updateTransaction(id, transactionRequest)
                .map(ResponseEntity::ok);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Single<ResponseEntity<Void>> deleteTransaction(String id) {
        return transactionService.deleteTransaction(id)
                .toSingleDefault(ResponseEntity.noContent().<Void>build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Single<ResponseEntity<TransferResponse>> transferFunds(TransferRequest transferRequest) {
        return transferService.transfer(transferRequest)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }
}
