package libraryhub.listener;

import java.util.concurrent.atomic.AtomicLong;
import libraryhub.circulation.ShelfInspector;
import libraryhub.event.LoanOverdue;
import libraryhub.event.Observer;

/** Reacts to an overdue loan by asking the inspector to check the shelf the item belongs to. */
public final class OverdueAlert implements Observer<LoanOverdue> {

    private final ShelfInspector inspector;
    private final AtomicLong alerts = new AtomicLong();

    public OverdueAlert(ShelfInspector inspector) {
        this.inspector = inspector;
    }

    @Override
    public void onEvent(LoanOverdue event) {
        alerts.incrementAndGet();
        inspector.inspect(event.shelfId(), event.loanId());
    }

    public long alertCount() {
        return alerts.get();
    }
}
