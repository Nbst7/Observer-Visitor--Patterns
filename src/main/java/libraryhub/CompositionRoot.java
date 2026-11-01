package libraryhub;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import libraryhub.catalog.Catalog;
import libraryhub.circulation.CirculationDesk;
import libraryhub.circulation.Inventory;
import libraryhub.circulation.LoanLedger;
import libraryhub.circulation.OverdueScanner;
import libraryhub.circulation.ShelfInspector;
import libraryhub.concurrent.CirculationProducer;
import libraryhub.event.EventType;
import libraryhub.event.InspectionCompleted;
import libraryhub.event.ItemAvailability;
import libraryhub.event.ListenerRegistry;
import libraryhub.event.LoanOverdue;
import libraryhub.event.SectionSummary;
import libraryhub.listener.LibrarianPanel;
import libraryhub.listener.Notification;
import libraryhub.listener.OverdueAlert;
import libraryhub.listener.ReplenishmentDesk;
import libraryhub.listener.ReservationService;
import libraryhub.listener.SectionAggregator;
import libraryhub.listener.StatisticsCollector;


public final class CompositionRoot {

    private CompositionRoot() {
    }

    public static LibrarySystem build(AppConfig config, Catalog catalog, Clock clock) {
        ConcurrentLinkedQueue<Throwable> listenerErrors = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Notification> notifications = new ConcurrentLinkedQueue<>();

        Inventory inventory = new Inventory(catalog);
        LoanLedger ledger = new LoanLedger();

        CirculationDesk desk = new CirculationDesk(catalog, inventory, ledger,
                new ListenerRegistry<ItemAvailability>(listenerErrors::add), clock, config.loanDays());
        OverdueScanner scanner = new OverdueScanner(ledger, catalog,
                new ListenerRegistry<LoanOverdue>(listenerErrors::add), clock);
        ShelfInspector inspector = new ShelfInspector(catalog, inventory,
                new ListenerRegistry<InspectionCompleted>(listenerErrors::add),
                config.lowStockThreshold(), config.shippingPerCopy());
        SectionAggregator aggregator = new SectionAggregator(catalog,
                new ListenerRegistry<SectionSummary>(listenerErrors::add));

        StatisticsCollector statistics = new StatisticsCollector();
        LibrarianPanel panel = new LibrarianPanel();
        OverdueAlert overdueAlert = new OverdueAlert(inspector);
        ReplenishmentDesk replenishment = new ReplenishmentDesk();
        ReservationService reservations = new ReservationService(desk, notifications::add);

        // Chain: desk -> aggregator -> panel
        desk.subscribe(EventType.ITEM_CHECKED_OUT, aggregator);
        desk.subscribe(EventType.ITEM_RETURNED, aggregator);
        aggregator.subscribe(EventType.SECTION_SUMMARY, panel);
        // Statistics listens to everything.
        desk.subscribe(EventType.ITEM_CHECKED_OUT, statistics);
        desk.subscribe(EventType.ITEM_RETURNED, statistics);
        scanner.subscribe(EventType.LOAN_OVERDUE, statistics);
        aggregator.subscribe(EventType.SECTION_SUMMARY, statistics);
        inspector.subscribe(EventType.INSPECTION_COMPLETED, statistics);
        // Overdue loan -> alert -> shelf inspection -> panel and replenishment ticket
        scanner.subscribe(EventType.LOAN_OVERDUE, overdueAlert);
        scanner.subscribe(EventType.LOAN_OVERDUE, panel);
        inspector.subscribe(EventType.INSPECTION_COMPLETED, panel);
        inspector.subscribe(EventType.INSPECTION_COMPLETED, replenishment);

        return new LibrarySystem(config, catalog, inventory, ledger, desk, scanner, inspector, aggregator,
                statistics, panel, overdueAlert, replenishment, reservations, notifications, listenerErrors);
    }

    public static List<Runnable> producers(LibrarySystem system, List<String> itemIds) {
        return java.util.stream.IntStream.range(0, system.config().producerThreads())
                .<Runnable>mapToObj(n -> new CirculationProducer(system.desk(), itemIds,
                        system.config().pairsPerProducer(), "member-" + n))
                .toList();
    }
}
