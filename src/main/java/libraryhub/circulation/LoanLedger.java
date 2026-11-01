package libraryhub.circulation;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class LoanLedger {

    private final ConcurrentHashMap<String, Loan> openLoans = new ConcurrentHashMap<>();

    public void open(Loan loan) {
        if (openLoans.putIfAbsent(loan.id(), loan) != null) {
            throw new IllegalStateException("Loan already open: " + loan.id());
        }
    }

    public Optional<Loan> close(String loanId) {
        return Optional.ofNullable(openLoans.remove(loanId));
    }

    public List<Loan> openLoans() {
        return List.copyOf(openLoans.values());
    }

    public int openCount() {
        return openLoans.size();
    }
}
