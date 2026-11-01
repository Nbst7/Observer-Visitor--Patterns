package libraryhub.listener;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import libraryhub.event.Event;
import libraryhub.event.EventType;
import libraryhub.event.Observer;

public final class StatisticsCollector implements Observer<Event> {

    private final Map<EventType, LongAdder> counters;

    public StatisticsCollector() {
        Map<EventType, LongAdder> byType = new EnumMap<>(EventType.class);
        for (EventType type : EventType.values()) {
            byType.put(type, new LongAdder());
        }
        this.counters = Collections.unmodifiableMap(byType);
    }

    @Override
    public void onEvent(Event event) {
        counters.get(event.type()).increment();
    }

    public long count(EventType type) {
        return counters.get(type).sum();
    }

    public long total() {
        return counters.values().stream().mapToLong(LongAdder::sum).sum();
    }
}
