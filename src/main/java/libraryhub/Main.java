package libraryhub;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import libraryhub.catalog.Catalog;
import libraryhub.circulation.AdjustableClock;
import libraryhub.concurrent.ProducerPool;
import libraryhub.event.EventType;
import libraryhub.visitor.InventorySummaryVisitor;
import libraryhub.visitor.JsonExportVisitor;
import libraryhub.visitor.ReplacementCostVisitor;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws InterruptedException {
        AppConfig config = AppConfig.parse(args);
        Catalog catalog = new Catalog(DemoCatalog.create());
        AdjustableClock clock = new AdjustableClock(Instant.now(), ZoneId.systemDefault());
        LibrarySystem system = CompositionRoot.build(config, catalog, clock);

        // A member joins at runtime and waits for a copy of the Design Patterns book.
        system.desk().checkOut("bk-gof", "member-wait");
        system.reservations().reserve("bk-gof", "member-wait");

        // Producers: configured number of threads borrow and return items in parallel.
        List<String> busyItems = List.of("bk-clean", "bk-clrs", "eb-hf", "au-1984");
        ProducerPool pool = new ProducerPool(config.producerThreads());
        long start = System.nanoTime();
        pool.runToCompletion(CompositionRoot.producers(system, busyItems), Duration.ofSeconds(30));
        long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();
        System.out.printf("Producers: %d threads x %d pairs finished in %d ms%n",
                config.producerThreads(), config.pairsPerProducer(), millis);

        // The waiting member brings the book back; two other loans stay open and become overdue.
        system.ledger().openLoans().stream()
                .filter(loan -> loan.memberId().equals("member-wait"))
                .forEach(loan -> system.desk().returnItem(loan.id()));
        system.desk().checkOut("bk-gof", "member-late-1");
        system.desk().checkOut("au-pragmatic", "member-late-2");
        clock.advance(Duration.ofDays(config.loanDays() + 5));
        int overdue = system.scanner().scan();

        System.out.println("Overdue loans found: " + overdue);
        System.out.println("Events: checkedOut=" + system.statistics().count(EventType.ITEM_CHECKED_OUT)
                + " returned=" + system.statistics().count(EventType.ITEM_RETURNED)
                + " summaries=" + system.statistics().count(EventType.SECTION_SUMMARY)
                + " inspections=" + system.statistics().count(EventType.INSPECTION_COMPLETED));
        System.out.println("Notifications: " + system.notifications());
        System.out.println("Replenishment tickets: " + system.replenishment().tickets());
        System.out.println(system.panel().render());

        var library = catalog.library();
        System.out.println("Replacement cost of the library: "
                + library.accept(new ReplacementCostVisitor(config.shippingPerCopy())));
        System.out.println("Inventory summary: " + library.accept(new InventorySummaryVisitor()));
        System.out.println("JSON of shelf sh-cs-2: "
                + catalog.shelf("sh-cs-2").orElseThrow().accept(new JsonExportVisitor()));
        System.out.println("Listener errors: " + system.listenerErrors().size());
    }
}
