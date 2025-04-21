package course.concurrency.m3_shared.auction;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Notifier {

    private final Thread sender;
    private final BlockingQueue<Bid> bidQueue = new LinkedBlockingQueue<>();

    public Notifier() {
        sender = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    final var bid = bidQueue.take();
                    imitateSending(bid);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        sender.start();
    }

    public void sendOutdatedMessage(Bid bid) {
        bidQueue.add(bid);
    }

    public void shutdown() {
        sender.interrupt();
        bidQueue.clear();
    }

    private void imitateSending(Bid bid) {
        // don't remove this delay, deal with it properly
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
