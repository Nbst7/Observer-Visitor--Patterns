package libraryhub.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import libraryhub.AppConfig;
import libraryhub.CompositionRoot;
import libraryhub.LibrarySystem;
import libraryhub.TestCatalogs;
import libraryhub.circulation.Inventory;
import libraryhub.event.EventType;
import libraryhub.event.ItemAvailability;
import libraryhub.event.ListenerRegistry;
import libraryhub.event.Observer;
import org.junit.jupiter.api.Test;

class ConcurrencyTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final List<String> ITEMS = List.of("b1", "j1", "b2");

    private static LibrarySystem system(int threads, int pairs) {
        return CompositionRoot.build(new AppConfig(threads, pairs, 14, 1, 2.0),
                TestCatalogs.catalogWithManyCopies(), Clock.systemUTC());
    }

    @Test
    void stressFourThreadsTimesOneThousandEventsGivesExactCounts() throws InterruptedException {
        // 4 producers x 500 pairs (check-out + return) = 4 x 1,000 events = 4,000 events.
        LibrarySystem system = system(4, 500);

        new ProducerPool(4).runToCompletion(CompositionRoot.producers(system, ITEMS), TIMEOUT);

        assertEquals(2000, system.statistics().count(EventType.ITEM_CHECKED_OUT));
        assertEquals(2000, system.statistics().count(EventType.ITEM_RETURNED));
        assertEquals(4000, system.statistics().count(EventType.SECTION_SUMMARY)); // the chained observer saw all
        assertEquals(8000, system.statistics().total());
        assertEquals(0, system.ledger().openCount());
        assertTrue(system.listenerErrors().isEmpty());
    }

    @Test
    void stressLeavesTheChainedSummariesExactlyConsistent() throws InterruptedException {
        LibrarySystem system = system(4, 500);

        new ProducerPool(4).runToCompletion(CompositionRoot.producers(system, ITEMS), TIMEOUT);

        long updates = 0;
        for (String sectionId : system.catalog().sectionIds()) {
            var summary = system.panel().summary(sectionId).orElseThrow();
            assertEquals(summary.checkouts(), summary.returns());
            assertEquals(0, summary.copiesOut());
            updates += summary.updates();
        }
        assertEquals(4000, updates); // out-of-order delivery did not leave a stale summary behind
        assertEquals(50, system.inventory().available("b1"));
    }

    @Test
    void subscribingAndUnsubscribingWhilePublishingNeverLosesEventsOfAStableListener() throws InterruptedException {
        ListenerRegistry<ItemAvailability> registry = new ListenerRegistry<>(error -> {
            throw new AssertionError(error);
        });
        AtomicInteger stable = new AtomicInteger();
        registry.subscribe(EventType.ITEM_RETURNED, e -> stable.incrementAndGet());
        AtomicBoolean publishing = new AtomicBoolean(true);

        Thread churn = new Thread(() -> {
            Observer<ItemAvailability> temporary = e -> { };
            while (publishing.get()) {
                registry.subscribe(EventType.ITEM_RETURNED, temporary);
                registry.unsubscribe(EventType.ITEM_RETURNED, temporary);
            }
        });
        churn.start();
        ExecutorService publishers = Executors.newFixedThreadPool(4);
        for (int t = 0; t < 4; t++) {
            publishers.submit(() -> {
                for (int i = 0; i < 1000; i++) {
                    registry.publish(new ItemAvailability(EventType.ITEM_RETURNED, "b1", "s", "sh", "m", 1));
                }
            });
        }
        publishers.shutdown();
        assertTrue(publishers.awaitTermination(30, TimeUnit.SECONDS));
        publishing.set(false);
        churn.join();

        assertEquals(4000, stable.get());
        assertEquals(1, registry.listenerCount(EventType.ITEM_RETURNED));
    }

    @Test
    void reservationWatchesNotifyExactlyOncePerMemberUnderConcurrentReturns() throws InterruptedException {
        LibrarySystem system = system(4, 100);
        for (int member = 0; member < 20; member++) {
            system.reservations().reserve("b1", "waiting-" + member);
        }

        new ProducerPool(4).runToCompletion(CompositionRoot.producers(system, List.of("b1")), TIMEOUT);

        assertEquals(20, system.notifications().size());
        assertEquals(20, system.notifications().stream().map(n -> n.memberId()).distinct().count());
    }

    @Test
    void inventoryNeverGoesNegativeOrAboveTotalUnderContention() throws InterruptedException {
        Inventory inventory = new Inventory(TestCatalogs.catalog()); // a1 has exactly 1 copy
        AtomicInteger successfulCheckouts = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService threads = Executors.newFixedThreadPool(8);
        for (int t = 0; t < 8; t++) {
            threads.submit(() -> {
                start.await();
                if (inventory.tryCheckOut("a1").isPresent()) {
                    successfulCheckouts.incrementAndGet();
                }
                return null;
            });
        }
        start.countDown();
        threads.shutdown();
        assertTrue(threads.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(1, successfulCheckouts.get());
        assertEquals(0, inventory.available("a1"));
        assertTrue(inventory.tryReturn("a1").isPresent());
        assertTrue(inventory.tryReturn("a1").isEmpty()); // cannot return more copies than owned
    }

    @Test
    void shutdownWaitsForAllProducersAndLeavesNoThreadsBehind() throws InterruptedException {
        AtomicInteger finished = new AtomicInteger();
        List<Runnable> producers = java.util.stream.IntStream.range(0, 6)
                .<Runnable>mapToObj(n -> () -> {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    finished.incrementAndGet();
                }).toList();

        new ProducerPool(3).runToCompletion(producers, TIMEOUT);

        assertEquals(6, finished.get()); // no task was dropped
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (producerThreadsAlive() > 0 && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        assertEquals(0, producerThreadsAlive());
    }

    @Test
    void shutdownGivesUpAndReportsProducersThatNeverFinish() {
        CountDownLatch never = new CountDownLatch(1);
        List<Runnable> stuck = List.of(() -> {
            try {
                never.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertThrows(IllegalStateException.class,
                () -> new ProducerPool(1).runToCompletion(stuck, Duration.ofMillis(200)));
    }

    @Test
    void producerFailuresSurfaceInsteadOfDisappearing() {
        List<Runnable> failing = List.of(() -> {
            throw new IllegalArgumentException("bad producer");
        });

        var failure = assertThrows(IllegalStateException.class,
                () -> new ProducerPool(2).runToCompletion(failing, TIMEOUT));
        assertEquals("bad producer", failure.getCause().getMessage());
    }

    @Test
    void configurationIsValidatedAndParsedFromArguments() {
        assertThrows(IllegalArgumentException.class, () -> new ProducerPool(0));
        assertThrows(IllegalArgumentException.class, () -> new AppConfig(0, 1, 14, 1, 2.0));

        AppConfig parsed = AppConfig.parse(new String[] {"--threads=6", "--pairs=10"});
        assertEquals(6, parsed.producerThreads());
        assertEquals(10, parsed.pairsPerProducer());
        assertThrows(IllegalArgumentException.class, () -> AppConfig.parse(new String[] {"--nope"}));
    }

    private static long producerThreadsAlive() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(t -> t.getName().startsWith("producer-") && t.isAlive())
                .count();
    }
}
