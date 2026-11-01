package libraryhub.listener;

import java.util.List;

/** The "maintenance ticket" at the end of alert -> inspection -> ticket. */
public record ReplenishmentTicket(String shelfId, String triggerLoanId, List<String> itemIds, double shelfReplacementCost) {

    public ReplenishmentTicket {
        itemIds = List.copyOf(itemIds);
    }
}
