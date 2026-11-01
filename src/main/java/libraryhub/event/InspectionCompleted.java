package libraryhub.event;

import java.util.List;
import java.util.Objects;

public record InspectionCompleted(
        String shelfId,
        String triggerLoanId,
        int titlesInspected,
        int copiesInspected,
        List<String> lowStockItemIds,
        double replacementCost) implements Event {

    public InspectionCompleted {
        Objects.requireNonNull(shelfId, "shelfId");
        Objects.requireNonNull(triggerLoanId, "triggerLoanId");
        lowStockItemIds = List.copyOf(lowStockItemIds);
    }

    @Override
    public EventType type() {
        return EventType.INSPECTION_COMPLETED;
    }
}
