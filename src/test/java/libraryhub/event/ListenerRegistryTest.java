package libraryhub.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListenerRegistryTest {

    private List<Throwable> errors;
    private ListenerRegistry<ItemAvailability> registry;

    @BeforeEach
    void setUp() {
        errors = new ArrayList<>();
        registry = new ListenerRegistry<>(errors::add);
    }

    private static ItemAvailability event(EventType type) {
        return new ItemAvailability(type, "b1", "sec-sci", "sh-1", "m1", 2);
    }

    @Test
    void listenerReceivesOnlyTheTypesItSubscribedTo() {
        List<EventType> received = new ArrayList<>();
        registry.subscribe(EventType.ITEM_RETURNED, e -> received.add(e.type()));

        registry.publish(event(EventType.ITEM_CHECKED_OUT));
        registry.publish(event(EventType.ITEM_RETURNED));

        assertEquals(List.of(EventType.ITEM_RETURNED), received);
    }

    @Test
    void unsubscribedListenerGetsNothingMore() {
        AtomicInteger calls = new AtomicInteger();
        Observer<ItemAvailability> observer = e -> calls.incrementAndGet();
        registry.subscribe(EventType.ITEM_RETURNED, observer);
        registry.publish(event(EventType.ITEM_RETURNED));

        registry.unsubscribe(EventType.ITEM_RETURNED, observer);
        registry.publish(event(EventType.ITEM_RETURNED));

        assertEquals(1, calls.get());
    }

    @Test
    void subscribingTheSameObserverTwiceDeliversOnce() {
        AtomicInteger calls = new AtomicInteger();
        Observer<ItemAvailability> observer = e -> calls.incrementAndGet();
        registry.subscribe(EventType.ITEM_RETURNED, observer);
        registry.subscribe(EventType.ITEM_RETURNED, observer);

        registry.publish(event(EventType.ITEM_RETURNED));

        assertEquals(1, calls.get());
        assertEquals(1, registry.listenerCount(EventType.ITEM_RETURNED));
    }

    @Test
    void listenerMayUnsubscribeItselfInsideItsHandler() {
        AtomicInteger oneShotCalls = new AtomicInteger();
        AtomicInteger neighbourCalls = new AtomicInteger();
        Observer<ItemAvailability> oneShot = new Observer<>() {
            @Override
            public void onEvent(ItemAvailability event) {
                oneShotCalls.incrementAndGet();
                registry.unsubscribe(EventType.ITEM_RETURNED, this);
            }
        };
        registry.subscribe(EventType.ITEM_RETURNED, oneShot);
        registry.subscribe(EventType.ITEM_RETURNED, e -> neighbourCalls.incrementAndGet());

        registry.publish(event(EventType.ITEM_RETURNED));
        registry.publish(event(EventType.ITEM_RETURNED));

        assertEquals(1, oneShotCalls.get());
        assertEquals(2, neighbourCalls.get()); // the neighbour was not skipped by the removal
        assertTrue(errors.isEmpty());
    }

    @Test
    void listenerAddedInsideAHandlerStartsWithTheNextEvent() {
        AtomicInteger lateCalls = new AtomicInteger();
        registry.subscribe(EventType.ITEM_RETURNED, e ->
                registry.subscribe(EventType.ITEM_RETURNED, late -> lateCalls.incrementAndGet()));

        registry.publish(event(EventType.ITEM_RETURNED));
        assertEquals(0, lateCalls.get());

        registry.publish(event(EventType.ITEM_RETURNED));
        assertEquals(1, lateCalls.get());
    }

    @Test
    void failingListenerDoesNotStopTheOthers() {
        AtomicInteger healthyCalls = new AtomicInteger();
        registry.subscribe(EventType.ITEM_RETURNED, e -> {
            throw new IllegalStateException("boom");
        });
        registry.subscribe(EventType.ITEM_RETURNED, e -> healthyCalls.incrementAndGet());

        registry.publish(event(EventType.ITEM_RETURNED));

        assertEquals(1, healthyCalls.get());
        assertEquals(1, errors.size());
        assertEquals("boom", errors.get(0).getMessage());
    }

    @Test
    void availabilityEventRejectsForeignTypes() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemAvailability(EventType.LOAN_OVERDUE, "b1", "s", "sh", "m", 1));
    }

    @Test
    void sectionSummaryDerivesTotalsFromItsCounters() {
        SectionSummary summary = new SectionSummary("sec-sci", 7, 5);
        assertEquals(12, summary.updates());
        assertEquals(2, summary.copiesOut());
        assertEquals(EventType.SECTION_SUMMARY, summary.type());
    }
}
