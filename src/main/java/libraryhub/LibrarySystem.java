package libraryhub;

import java.util.Queue;
import libraryhub.catalog.Catalog;
import libraryhub.circulation.CirculationDesk;
import libraryhub.circulation.Inventory;
import libraryhub.circulation.LoanLedger;
import libraryhub.circulation.OverdueScanner;
import libraryhub.circulation.ShelfInspector;
import libraryhub.listener.LibrarianPanel;
import libraryhub.listener.Notification;
import libraryhub.listener.OverdueAlert;
import libraryhub.listener.ReplenishmentDesk;
import libraryhub.listener.ReservationService;
import libraryhub.listener.SectionAggregator;
import libraryhub.listener.StatisticsCollector;

/** The fully wired object graph produced by {@link CompositionRoot}. */
public record LibrarySystem(
        AppConfig config,
        Catalog catalog,
        Inventory inventory,
        LoanLedger ledger,
        CirculationDesk desk,
        OverdueScanner scanner,
        ShelfInspector inspector,
        SectionAggregator aggregator,
        StatisticsCollector statistics,
        LibrarianPanel panel,
        OverdueAlert overdueAlert,
        ReplenishmentDesk replenishment,
        ReservationService reservations,
        Queue<Notification> notifications,
        Queue<Throwable> listenerErrors) {
}
