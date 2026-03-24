package nttdata.bootcamp.transactions_service.Service;

import com.bank.transaction.model.TransferRequest;
import com.bank.transaction.model.TransferResponse;
import io.reactivex.rxjava3.core.Single;

/**
 * Service for handling bank transfers between accounts.
 * Supports same-customer and third-party transfers within the same bank.
 */
public interface TransferService {

    /**
     * Executes a transfer from source account to target account.
     * Records two transactions: a WITHDRAWAL on the source and a DEPOSIT on the target.
     * Commission fee is applied if the source account has exceeded its free transaction limit.
     */
    Single<TransferResponse> transfer(TransferRequest request);
}
