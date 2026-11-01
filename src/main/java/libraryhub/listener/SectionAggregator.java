package libraryhub.listener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import libraryhub.catalog.Catalog;
import libraryhub.event.EventType;
import libraryhub.event.ItemAvailability;
import libraryhub.event.ListenerRegistry;
import libraryhub.event.Observer;
import libraryhub.event.SectionSummary;
import libraryhub.event.Subject;


public final class SectionAggregator implements Observer<ItemAvailability>, Subject<SectionSummary> {

    private record Totals(long checkouts, long returns) {
        Totals apply(EventType type) {
            return type == EventType.ITEM_CHECKED_OUT
                    ? new Totals(checkouts + 1, returns)
                    : new Totals(checkouts, returns + 1);
        }
    }

    private final Map<String, Totals> totals;
    private final ListenerRegistry<SectionSummary> listeners;

    public SectionAggregator(Catalog catalog, ListenerRegistry<SectionSummary> listeners) {
        Map<String, Totals> initial = new HashMap<>();
        for (String sectionId : catalog.sectionIds()) {
            initial.put(sectionId, new Totals(0, 0));
        }
        this.totals = new ConcurrentHashMap<>(Collections.unmodifiableMap(initial));
        this.listeners = listeners;
    }

    @Override
    public void onEvent(ItemAvailability event) {
        Totals updated = totals.compute(event.sectionId(), (id, old) -> old.apply(event.type()));
        listeners.publish(new SectionSummary(event.sectionId(), updated.checkouts(), updated.returns()));
    }

    @Override
    public void subscribe(EventType type, Observer<? super SectionSummary> observer) {
        listeners.subscribe(type, observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<? super SectionSummary> observer) {
        listeners.unsubscribe(type, observer);
    }
}
