package libraryhub.concurrent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public final class ProducerPool {

    private final ExecutorService executor;

    public ProducerPool(int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("threads must be at least 1");
        }
        AtomicInteger counter = new AtomicInteger();
        this.executor = Executors.newFixedThreadPool(threads,
                task -> new Thread(task, "producer-" + counter.incrementAndGet()));
    }

    public void runToCompletion(List<? extends Runnable> producers, Duration timeout) throws InterruptedException {
        List<Future<?>> futures = new ArrayList<>();
        for (Runnable producer : producers) {
            futures.add(executor.submit(producer));
        }
        executor.shutdown(); // no new tasks; already submitted ones still run, so no event is dropped
        if (!executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            executor.shutdownNow();
            throw new IllegalStateException("Producers did not finish within " + timeout);
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException failure) {
                throw new IllegalStateException("A producer failed", failure.getCause());
            }
        }
    }
}
