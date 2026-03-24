package nttdata.bootcamp.transactions_service.Service.Implement;

import com.bank.transaction.model.TransactionRequest;
import com.bank.transaction.model.TransactionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

import nttdata.bootcamp.transactions_service.Domain.Policy.Transaction.TransactionProductPolicyRegistry;
import nttdata.bootcamp.transactions_service.Mapper.TransactionMapper;
import nttdata.bootcamp.transactions_service.Repository.TransactionRepository;
import nttdata.bootcamp.transactions_service.Service.TransactionService;
import nttdata.bootcamp.transactions_service.Validation.ValidationSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * Default {@link TransactionService}: MongoDB persistence, product policies and Redis list cache per product.
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    @Value("${redis.cache.ttl-seconds:900}")
    private long cacheTtlSeconds;

    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final ValidationSupport validationSupport;
    private final TransactionProductPolicyRegistry transactionProductPolicyRegistry;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Drops cached transaction list for a product id.
     *
     * @param productId product key used in cache key
     * @return completion when delete finishes (or empty if {@code productId} is null)
     */
    private Mono<Void> evictTransactionsByProductCache(String productId) {
        if (productId == null) {
            return Mono.empty();
        }
        String key = "transactionsByProduct:" + productId;
        return redisTemplate.delete(key).then();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Single<List<TransactionResponse>> findAll() {
        return RxJava3Adapter.fluxToFlowable(repository.findAll())
                .map(mapper::toDTO)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Single<List<TransactionResponse>> getTransactionsByProduct(String productId) {
        String cacheKey = "transactionsByProduct:" + productId;

        Mono<List<TransactionResponse>> fromCache = redisTemplate.opsForValue().get(cacheKey)
                .flatMap(json -> {
                    try {
                        return Mono.just(objectMapper.readValue(
                                json,
                                new TypeReference<List<TransactionResponse>>() {
                                }
                        ));
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(
                                "Failed to deserialize transactions list from cache", e));
                    }
                });

        Mono<List<TransactionResponse>> loadAndCache = repository.findByProductId(productId)
                .map(mapper::toDTO)
                .collectList()
                .flatMap(list -> {
                    try {
                        String json = objectMapper.writeValueAsString(list);
                        return redisTemplate.opsForValue()
                                .set(cacheKey, json, Duration.ofSeconds(cacheTtlSeconds))
                                .thenReturn(list);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(
                                "Failed to serialize transactions list for cache", e));
                    }
                });

        return RxJava3Adapter.monoToSingle(
                fromCache.switchIfEmpty(loadAndCache)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Single<TransactionResponse> createTransaction(TransactionRequest request) {
        return RxJava3Adapter.monoToSingle(
                Mono.fromCallable(() -> validationSupport.validateOrThrow(request))
                        .then(Mono.defer(() -> transactionProductPolicyRegistry
                                .resolve(request.getProductType().getValue())
                                .prepareDocument(request)))
                        .flatMap(repository::save)
                        .map(mapper::toDTO)
                        .flatMap(resp -> evictTransactionsByProductCache(resp.getProductId()).thenReturn(resp))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Maybe<TransactionResponse> getTransactionById(String id) {
        return RxJava3Adapter.monoToMaybe(repository.findById(id))
                .map(mapper::toDTO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Single<TransactionResponse> updateTransaction(String id, TransactionRequest request) {
        return RxJava3Adapter.monoToSingle(
                Mono.fromCallable(() -> validationSupport.validateOrThrow(request))
                        .then(repository.findById(id)
                                .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found")))
                                .doOnNext(doc -> mapper.updateDocument(doc, request))
                                .flatMap(repository::save)
                                .map(mapper::toDTO)
                                .flatMap(resp -> evictTransactionsByProductCache(resp.getProductId()).thenReturn(resp))
                        )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Completable deleteTransaction(String id) {
        return RxJava3Adapter.monoToMaybe(repository.findById(id))
                .switchIfEmpty(Single.error(new RuntimeException("Transaction not found")))
                .flatMapCompletable(doc -> RxJava3Adapter.monoToCompletable(
                        repository.delete(doc).then(evictTransactionsByProductCache(doc.getProductId()))
                ));
    }

}
