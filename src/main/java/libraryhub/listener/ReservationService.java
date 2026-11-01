package libraryhub.listener;

import java.util.function.Consumer;
import libraryhub.event.EventType;
import libraryhub.event.ItemAvailability;
import libraryhub.event.Subject;

public final class ReservationService {

    private final Subject<ItemAvailability> source;
    private final Consumer<Notification> notifications;

    public ReservationService(Subject<ItemAvailability> source, Consumer<Notification> notifications) {
        this.source = source;
        this.notifications = notifications;
    }

    public void reserve(String itemId, String memberId) {
        source.subscribe(EventType.ITEM_RETURNED, new ReservationWatch(itemId, memberId, source, notifications));
    }
}
