package libraryhub.event;

import java.util.Objects;

public record LoanOverdue(
        String loanId,
        String itemId,
        String sectionId,
        String shelfId,
        String memberId,
        long daysOverdue) implements Event {

    public LoanOverdue {
        Objects.requireNonNull(loanId, "loanId");
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(sectionId, "sectionId");
        Objects.requireNonNull(shelfId, "shelfId");
        Objects.requireNonNull(memberId, "memberId");
        if (daysOverdue < 1) {
            throw new IllegalArgumentException("daysOverdue must be at least 1");
        }
    }
    @Override
    public EventType type() {
        return EventType.LOAN_OVERDUE;
    }
}
