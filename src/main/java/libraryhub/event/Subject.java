package libraryhub.event;

/**
 * Something that notifies observers. {@code Observer<? super E>} lets one
 * {@code Observer<Event>} listen to subjects that publish different event records.
 */
public interface Subject<E extends Event> {

    void subscribe(EventType type, Observer<? super E> observer);

    void unsubscribe(EventType type, Observer<? super E> observer);
}
