package libraryhub.listener;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import libraryhub.event.InspectionCompleted;
import libraryhub.event.Observer;

/** Opens a ticket whenever an inspection found items that are almost out of stock. */
public final class ReplenishmentDesk implements Observer<InspectionCompleted> {

    private final ConcurrentLinkedQueue<ReplenishmentTicket> tickets = new ConcurrentLinkedQueue<>();

    @Override
    public void onEvent(InspectionCompleted event) {
        if (!event.lowStockItemIds().isEmpty()) {
            tickets.add(new ReplenishmentTicket(event.shelfId(), event.triggerLoanId(),
                    event.lowStockItemIds(), event.replacementCost()));
        }
    }

    public List<ReplenishmentTicket> tickets() {
        return List.copyOf(tickets);
    }
}
