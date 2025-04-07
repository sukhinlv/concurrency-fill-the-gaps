package course.concurrency.m2_async.minPrice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static org.mockito.internal.util.StringUtil.join;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10L, 45L, 66L, 345L, 234L, 333L, 67L, 123L, 768L);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        // TODO плохое решение, но иначе не укладываюсь в SLA никак.
        //  Здесь - чтобы проходили тесты, потому что минимальная цена приходит из последнего магазина.
        ExecutorService executor = Executors.newFixedThreadPool(shopIds.size());
        var minPrice = Double.NaN;
        try {
            final var requests = new ArrayList<CompletableFuture<Double>>();
            shopIds.forEach(shopId -> requests.add(CompletableFuture
                    .supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), executor)
                    .orTimeout(2800, TimeUnit.MILLISECONDS)
                    .exceptionally(throwable -> Double.NaN)));
            minPrice = CompletableFuture.allOf(requests.toArray(new CompletableFuture<?>[0]))
                    .thenApply(v -> requests.stream()
                            .map(CompletableFuture::join)
                            .filter(not(aDouble -> aDouble.isNaN()))
                            .min(Double::compareTo)
                            .orElse(Double.NaN))
                    .orTimeout(2900, TimeUnit.MILLISECONDS)
                    .exceptionally(throwable -> Double.NaN)
                    .join();
        } finally {
            executor.shutdown();
        }
        return minPrice;
    }
}
