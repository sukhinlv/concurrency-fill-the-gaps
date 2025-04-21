package course.concurrency.m3_shared.auction;

import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.isNull;

public class AuctionOptimistic implements Auction {

    private final Notifier notifier;
    private final AtomicReference<Bid> latestBid = new AtomicReference<>(null);

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    public boolean propose(Bid newBid) {
        final var updatedBid = latestBid.updateAndGet(savedBid -> {
            if (isNull(savedBid) || (newBid.getPrice() > savedBid.getPrice())) {
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
}
