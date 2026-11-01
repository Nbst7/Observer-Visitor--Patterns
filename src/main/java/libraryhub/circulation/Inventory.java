package libraryhub.circulation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
import libraryhub.catalog.Catalog;
import libraryhub.catalog.Item;


public final class Inventory {

    private final Map<String, AtomicInteger> available;
    private final Map<String, Integer> totals;

    public Inventory(Catalog catalog) {
        Map<String, AtomicInteger> counters = new HashMap<>();
        Map<String, Integer> totalCopies = new HashMap<>();
        for (Item item : catalog.items()) {
            counters.put(item.id(), new AtomicInteger(item.copies()));
            totalCopies.put(item.id(), item.copies());
        }
        this.available = Collections.unmodifiableMap(counters);
        this.totals = Collections.unmodifiableMap(totalCopies);
    }

    public int available(String itemId) {
        return counter(itemId).get();
    }

    public OptionalInt tryCheckOut(String itemId) {
        return adjust(itemId, -1);
    }

    public OptionalInt tryReturn(String itemId) {
        return adjust(itemId, +1);
    }

    private OptionalInt adjust(String itemId, int delta) {
        AtomicInteger counter = counter(itemId);
        int max = totals.get(itemId);
        while (true) {
            int current = counter.get();
            int next = current + delta;
            if (next < 0 || next > max) {
                return OptionalInt.empty();
            }
            if (counter.compareAndSet(current, next)) {
                return OptionalInt.of(next);
            }
        }
    }

    private AtomicInteger counter(String itemId) {
        AtomicInteger counter = available.get(itemId);
        if (counter == null) {
            throw new IllegalArgumentException("Unknown item: " + itemId);
        }
        return counter;
    }
}
