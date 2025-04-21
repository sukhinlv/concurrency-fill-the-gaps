package course.concurrency.m3_shared.auction;

import static java.util.Objects.isNull;

public class AuctionPessimistic implements Auction {

    private final Notifier notifier;
    private volatile Bid latestBid;

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    public boolean propose(Bid bid) {
        final var bidProposed = proposeBid(bid);
        if (bidProposed) {
            notifier.sendOutdatedMessage(latestBid);
        }
        return bidProposed;
    }

    private synchronized boolean proposeBid(Bid bid) {
        if (isNull(latestBid) || (bid.getPrice() > latestBid.getPrice())) {
            latestBid = bid;
            return true;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }
}
