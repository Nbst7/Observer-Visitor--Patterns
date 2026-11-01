package libraryhub.listener;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import libraryhub.event.Event;
import libraryhub.event.InspectionCompleted;
import libraryhub.event.LoanOverdue;
import libraryhub.event.Observer;
import libraryhub.event.SectionSummary;

public final class LibrarianPanel implements Observer<Event> {

    private final Map<String, SectionSummary> latestSummaries = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String> log = new ConcurrentLinkedQueue<>();

    @Override
    public void onEvent(Event event) {
        if (event instanceof SectionSummary summary) {
            // Events from different threads may arrive out of order: keep the one with more updates.
            latestSummaries.merge(summary.sectionId(), summary,
                    (old, fresh) -> fresh.updates() > old.updates() ? fresh : old);
        } else if (event instanceof LoanOverdue overdue) {
            log.add("OVERDUE " + overdue.loanId() + " item=" + overdue.itemId()
                    + " member=" + overdue.memberId() + " days=" + overdue.daysOverdue());
        } else if (event instanceof InspectionCompleted inspection) {
            log.add("INSPECTED " + inspection.shelfId() + " titles=" + inspection.titlesInspected()
                    + " lowStock=" + inspection.lowStockItemIds()
                    + " replacement=" + inspection.replacementCost());
        }
    }

    public Optional<SectionSummary> summary(String sectionId) {
        return Optional.ofNullable(latestSummaries.get(sectionId));
    }

    public List<String> logLines() {
        return List.copyOf(log);
    }

    public String render() {
        StringBuilder screen = new StringBuilder("=== Librarian panel ===\n");
        latestSummaries.values().stream()
                .sorted(Comparator.comparing(SectionSummary::sectionId))
                .forEach(s -> screen.append(String.format("%-10s checkouts=%d returns=%d out=%d%n",
                        s.sectionId(), s.checkouts(), s.returns(), s.copiesOut())));
        log.forEach(line -> screen.append(line).append('\n'));
        return screen.toString();
    }
}
