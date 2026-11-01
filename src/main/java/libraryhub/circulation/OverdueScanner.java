package libraryhub.circulation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import libraryhub.catalog.Catalog;
import libraryhub.catalog.Location;
import libraryhub.event.EventType;
import libraryhub.event.ListenerRegistry;
import libraryhub.event.LoanOverdue;
import libraryhub.event.Observer;
import libraryhub.event.Subject;

public final class OverdueScanner implements Subject<LoanOverdue> {

    private final LoanLedger ledger;
    private final Catalog catalog;
    private final ListenerRegistry<LoanOverdue> listeners;
    private final Clock clock;

    public OverdueScanner(LoanLedger ledger, Catalog catalog, ListenerRegistry<LoanOverdue> listeners, Clock clock) {
        this.ledger = ledger;
        this.catalog = catalog;
        this.listeners = listeners;
        this.clock = clock;
    }

    public int scan() {
        LocalDate today = LocalDate.now(clock);
        int published = 0;
        for (Loan loan : ledger.openLoans()) {
            if (today.isAfter(loan.dueDate())) {
                Location location = catalog.locate(loan.itemId()).orElseThrow();
                listeners.publish(new LoanOverdue(loan.id(), loan.itemId(), location.sectionId(),
                        location.shelfId(), loan.memberId(), ChronoUnit.DAYS.between(loan.dueDate(), today)));
                published++;
            }
        }
        return published;
    }

    @Override
    public void subscribe(EventType type, Observer<? super LoanOverdue> observer) {
        listeners.subscribe(type, observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<? super LoanOverdue> observer) {
        listeners.unsubscribe(type, observer);
    }
}
