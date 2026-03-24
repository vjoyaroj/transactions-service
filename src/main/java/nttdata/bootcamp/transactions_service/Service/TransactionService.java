package nttdata.bootcamp.transactions_service.Service;

import com.bank.transaction.model.TransactionRequest;
import com.bank.transaction.model.TransactionResponse;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * Reactive contract for transaction CRUD and product-scoped listings (with optional Redis cache).
 */
public interface TransactionService {

    /**
     * Returns all stored transactions.
     */
    Single<List<TransactionResponse>> findAll();

    /**
     * Returns transactions for a product id (cached by product when configured).
     *
     * @param productId account, card or credit product identifier
     */
    Single<List<TransactionResponse>> getTransactionsByProduct(String productId);

    /**
     * Creates a transaction after validation and product policy preparation.
     */
    Single<TransactionResponse> createTransaction(TransactionRequest request);

    /**
     * Loads a single transaction by id.
     */
    Maybe<TransactionResponse> getTransactionById(String id);

    /**
     * Updates an existing transaction.
     */
    Single<TransactionResponse> updateTransaction(String id, TransactionRequest request);

    /**
     * Deletes a transaction and evicts related cache entries.
     */
    Completable deleteTransaction(String id);

}
