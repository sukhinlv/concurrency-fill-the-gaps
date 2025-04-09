package course.concurrency.m3_shared;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Counter {

    private static final int THREAD_COUNT = 3;
    public static final int MAX_ITERATIONS = 3;
    public static final Object LOCK = new Object();
    private static final List<Integer> counters = IntStream.range(0, THREAD_COUNT)
            .boxed()
            .map(unused -> 0)
            .collect(Collectors.toList());

    private static int pointer = 1;

    private static void processThreadWithNumber(final int threadNumber) {
        final var index = threadNumber - 1;
        while (counters.get(index) < MAX_ITERATIONS) {
            synchronized (LOCK) {
                while (pointer != threadNumber) {
                    try {
                        LOCK.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println(Thread.currentThread().getName() + " " + threadNumber);
                pointer = 1 + (pointer % THREAD_COUNT);
                counters.set(index, counters.get(index) + 1);
                LOCK.notifyAll();
            }
        }
    }

    public static void first() {
        processThreadWithNumber(1);
    }

    public static void second() {
        processThreadWithNumber(2);
    }

    public static void third() {
        processThreadWithNumber(3);
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> first());
        Thread t2 = new Thread(() -> second());
        Thread t3 = new Thread(() -> third());
        t1.setName("Thread-1");
        t1.start();
        t2.setName("Thread-2");
        t2.start();
        t3.setName("Thread-3");
        t3.start();
    }
}
