package course.concurrency.m3_shared.auction;

import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.isNull;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private final Notifier notifier;
    private final AtomicReference<Bid> latestBid = new AtomicReference<>(null);
private volatile boolean stopped = false;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    public boolean propose(Bid newBid) {
        final var updatedBid = latestBid.updateAndGet(savedBid -> {
            // .. && !stopped - порядок важен, потому что возможно длительное получение цены
            if ((isNull(savedBid) || (newBid.getPrice() > savedBid.getPrice())) && !stopped) {
                return newBid;
            }
            return savedBid;
        });
        final var bidUpdated = updatedBid.getPrice().equals(newBid.getPrice());
        if (bidUpdated) {
            notifier.sendOutdatedMessage(newBid);
        }
        return bidUpdated;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }

    public Bid stopAuction() {
        stopped = true;
        return latestBid.get();
    }
}
