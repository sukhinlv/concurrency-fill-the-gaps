package course.concurrency.m3_shared.auction;

import static java.util.Objects.isNull;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private final Notifier notifier;
    private volatile Bid latestBid;
    private volatile boolean stopped = false;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    public boolean propose(Bid bid) {
        if (stopped) return false;
        final var bidProposed = proposeBid(bid);
        if (bidProposed) {
            notifier.sendOutdatedMessage(latestBid);
        }
        return bidProposed;
    }

    private synchronized boolean proposeBid(Bid bid) {
        // .. && !stopped - порядок важен, потому что возможно длительное получение цены
        if ((isNull(latestBid) || (bid.getPrice() > latestBid.getPrice())) && !stopped) {
            latestBid = bid;
            return true;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }

    public Bid stopAuction() {
        stopped = true;
        return latestBid;
    }
}
