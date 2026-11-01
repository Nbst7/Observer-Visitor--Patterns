package libraryhub.listener;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import libraryhub.event.EventType;
import libraryhub.event.ItemAvailability;
import libraryhub.event.Observer;
import libraryhub.event.Subject;

public final class ReservationWatch implements Observer<ItemAvailability> {

    private final String itemId;
    private final String memberId;
    private final Subject<ItemAvailability> source;
    private final Consumer<Notification> notifications;
    private final AtomicBoolean done = new AtomicBoolean();

    public ReservationWatch(String itemId, String memberId, Subject<ItemAvailability> source,
                            Consumer<Notification> notifications) {
        this.itemId = itemId;
        this.memberId = memberId;
        this.source = source;
        this.notifications = notifications;
    }

    @Override
    public void onEvent(ItemAvailability event) {
        if (event.itemId().equals(itemId) && event.copiesAvailable() > 0 && done.compareAndSet(false, true)) {
            notifications.accept(new Notification(memberId, itemId));
            source.unsubscribe(EventType.ITEM_RETURNED, this);
        }
    }
}
