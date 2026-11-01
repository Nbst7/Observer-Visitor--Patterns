package libraryhub.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import libraryhub.AppConfig;
import libraryhub.CompositionRoot;
import libraryhub.LibrarySystem;
import libraryhub.TestCatalogs;
import libraryhub.circulation.AdjustableClock;
import libraryhub.event.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListenerChainTest {

    private AdjustableClock clock;
    private LibrarySystem system;

    @BeforeEach
    void setUp() {
        clock = new AdjustableClock(Instant.parse("2026-10-01T10:00:00Z"), ZoneOffset.UTC);
        system = CompositionRoot.build(AppConfig.defaults(), TestCatalogs.catalog(), clock);
    }

    @Test
    void aggregatorRepublishesSectionTotalsToThePanel() {
        var loan = system.desk().checkOut("b1", "m1").orElseThrow(); // sec-sci
        system.desk().checkOut("b2", "m2");                           // sec-art
        system.desk().returnItem(loan.id());

        var science = system.panel().summary("sec-sci").orElseThrow();
        assertEquals(1, science.checkouts());
        assertEquals(1, science.returns());
        assertEquals(0, science.copiesOut());
        assertEquals(1, system.panel().summary("sec-art").orElseThrow().copiesOut());
    }

    @Test
    void reservationNotifiesOnceAndThenLeavesTheSubject() {
        var loan = system.desk().checkOut("a1", "m-holder").orElseThrow();
        system.reservations().reserve("a1", "m-waiting");

        system.desk().returnItem(loan.id());
        var second = system.desk().checkOut("a1", "m-other").orElseThrow();
        system.desk().returnItem(second.id());

        assertEquals(List.of(new Notification("m-waiting", "a1")), List.copyOf(system.notifications()));
    }

    @Test
    void reservationIgnoresOtherItems() {
        system.reservations().reserve("a1", "m-waiting");

        var other = system.desk().checkOut("b1", "m1").orElseThrow();
        system.desk().returnItem(other.id());

        assertTrue(system.notifications().isEmpty());
    }

    @Test
    void statisticsCountEveryEventTypeIncludingChainedOnes() {
        var loan = system.desk().checkOut("b1", "m1").orElseThrow();
        system.desk().returnItem(loan.id());

        assertEquals(1, system.statistics().count(EventType.ITEM_CHECKED_OUT));
        assertEquals(1, system.statistics().count(EventType.ITEM_RETURNED));
        assertEquals(2, system.statistics().count(EventType.SECTION_SUMMARY));
        assertEquals(4, system.statistics().total());
    }

    @Test
    void overdueLoanTriggersInspectionOfThatShelfOnly() {
        system.desk().checkOut("b1", "m-late").orElseThrow(); // lives on sh-1 together with j1
        clock.advance(Duration.ofDays(20));

        assertEquals(1, system.scanner().scan());

        assertEquals(1, system.overdueAlert().alertCount());
        assertEquals(1, system.statistics().count(EventType.INSPECTION_COMPLETED));
        // sh-1 holds b1 (3 copies) and j1 (2 copies): the other shelves were not visited.
        var inspection = system.panel().logLines().stream().filter(l -> l.startsWith("INSPECTED")).findFirst().orElseThrow();
        assertTrue(inspection.contains("sh-1 titles=2"), inspection);
        assertTrue(inspection.contains("replacement=50.0"), inspection); // hand-calculated: 36 + 14
    }

    @Test
    void inspectionResultComesBackAsATicketForLowStockItems() {
        system.desk().checkOut("b1", "m1");
        system.desk().checkOut("b1", "m2");
        system.desk().checkOut("b1", "m3"); // all 3 copies of b1 are out -> available 0
        clock.advance(Duration.ofDays(30));

        system.scanner().scan();

        var tickets = system.replenishment().tickets();
        assertEquals(3, tickets.size()); // one inspection (and ticket) per overdue loan on the shelf
        assertEquals(List.of("b1"), tickets.get(0).itemIds()); // threshold is 1; j1 still has 2 copies
        assertEquals("sh-1", tickets.get(0).shelfId());
    }

    @Test
    void ticketListsOnlyTheLowStockItemsOfTheInspectedShelf() {
        system.desk().checkOut("b2", "m1"); // sh-3: b2 has 4 copies, a1 has 1 copy
        clock.advance(Duration.ofDays(30));

        system.scanner().scan();

        // a1 has 1 copy (<= threshold 1) and is listed; b2 still has 3 copies and is not
        assertEquals(List.of("a1"), system.replenishment().tickets().get(0).itemIds());
        assertTrue(system.listenerErrors().isEmpty());
    }
}
