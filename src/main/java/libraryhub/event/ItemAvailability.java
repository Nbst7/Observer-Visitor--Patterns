package libraryhub.event;

import java.util.Objects;

/** A copy of an item left or came back to the shelf. {@code type} is ITEM_CHECKED_OUT or ITEM_RETURNED. */
public record ItemAvailability(
        EventType type,
        String itemId,
        String sectionId,
        String shelfId,
        String memberId,
        int copiesAvailable) implements Event {

    public ItemAvailability {
        if (type != EventType.ITEM_CHECKED_OUT && type != EventType.ITEM_RETURNED) {
            throw new IllegalArgumentException("Not an availability event type: " + type);
        }
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(sectionId, "sectionId");
        Objects.requireNonNull(shelfId, "shelfId");
        Objects.requireNonNull(memberId, "memberId");
        if (copiesAvailable < 0) {
            throw new IllegalArgumentException("copiesAvailable must not be negative");
        }
    }
}
