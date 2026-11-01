package libraryhub.listener;

import java.util.List;

public record ReplenishmentTicket(String shelfId, String triggerLoanId, List<String> itemIds, double shelfReplacementCost) {

    public ReplenishmentTicket {
        itemIds = List.copyOf(itemIds);
    }
}
