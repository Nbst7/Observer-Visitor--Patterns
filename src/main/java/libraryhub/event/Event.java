package libraryhub.event;

/**
 * An immutable fact published by a {@link Subject}. Implementations are records,
 * so listeners can share one instance between threads without copying (push model).
 */
public interface Event {
    EventType type();
}
