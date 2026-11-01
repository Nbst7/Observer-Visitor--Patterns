package libraryhub.circulation;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicLong;
import libraryhub.catalog.Catalog;
import libraryhub.catalog.Location;
import libraryhub.event.EventType;
import libraryhub.event.ItemAvailability;
import libraryhub.event.ListenerRegistry;
import libraryhub.event.Observer;
import libraryhub.event.Subject;

public final class CirculationDesk implements Subject<ItemAvailability> {

    private final Catalog catalog;
    private final Inventory inventory;
    private final LoanLedger ledger;
    private final ListenerRegistry<ItemAvailability> listeners;
    private final Clock clock;
    private final int loanDays;
    private final AtomicLong loanSequence = new AtomicLong();

    public CirculationDesk(Catalog catalog, Inventory inventory, LoanLedger ledger,
                           ListenerRegistry<ItemAvailability> listeners, Clock clock, int loanDays) {
        this.catalog = catalog;
        this.inventory = inventory;
        this.ledger = ledger;
        this.listeners = listeners;
        this.clock = clock;
        this.loanDays = loanDays;
    }

    public Optional<Loan> checkOut(String itemId, String memberId) {
        Location location = locate(itemId);
        OptionalInt left = inventory.tryCheckOut(itemId);
        if (left.isEmpty()) {
            return Optional.empty();
        }
        Loan loan = new Loan("L-" + loanSequence.incrementAndGet(), itemId, memberId,
                LocalDate.now(clock).plusDays(loanDays));
        ledger.open(loan);
        listeners.publish(new ItemAvailability(EventType.ITEM_CHECKED_OUT, itemId,
                location.sectionId(), location.shelfId(), memberId, left.getAsInt()));
        return Optional.of(loan);
    }

    public boolean returnItem(String loanId) {
        Optional<Loan> closed = ledger.close(loanId);
        if (closed.isEmpty()) {
            return false;
        }
        Loan loan = closed.get();
        Location location = locate(loan.itemId());
        OptionalInt now = inventory.tryReturn(loan.itemId());
        if (now.isEmpty()) {
            throw new IllegalStateException("More copies returned than owned: " + loan.itemId());
        }
        listeners.publish(new ItemAvailability(EventType.ITEM_RETURNED, loan.itemId(),
                location.sectionId(), location.shelfId(), loan.memberId(), now.getAsInt()));
        return true;
    }

    @Override
    public void subscribe(EventType type, Observer<? super ItemAvailability> observer) {
        listeners.subscribe(type, observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<? super ItemAvailability> observer) {
        listeners.unsubscribe(type, observer);
    }

    private Location locate(String itemId) {
        return catalog.locate(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown item: " + itemId));
    }
}
