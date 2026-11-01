package libraryhub.event;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ListenerRegistry<E extends Event> implements Subject<E> {

    private final Map<EventType, CopyOnWriteArrayList<Observer<? super E>>> listeners;
    private final Consumer<Throwable> errorHandler;

    public ListenerRegistry(Consumer<Throwable> errorHandler) {
        this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler");
        Map<EventType, CopyOnWriteArrayList<Observer<? super E>>> byType = new EnumMap<>(EventType.class);
        for (EventType type : EventType.values()) {
            byType.put(type, new CopyOnWriteArrayList<>());
        }
        this.listeners = Collections.unmodifiableMap(byType);
    }

    @Override
    public void subscribe(EventType type, Observer<? super E> observer) {
        Objects.requireNonNull(observer, "observer");
        listeners.get(type).addIfAbsent(observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<? super E> observer) {
        listeners.get(type).remove(observer);
    }

    public void publish(E event) {
        for (Observer<? super E> observer : listeners.get(event.type())) {
            try {
                observer.onEvent(event);
            } catch (RuntimeException failure) {
                errorHandler.accept(failure); // one broken listener must not stop the others
            }
        }
    }

    public int listenerCount(EventType type) {
        return listeners.get(type).size();
    }
}
