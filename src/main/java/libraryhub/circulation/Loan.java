package libraryhub.circulation;

import java.time.LocalDate;

public record Loan(String id, String itemId, String memberId, LocalDate dueDate) {
}

