package libraryhub.circulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import libraryhub.AppConfig;
import libraryhub.CompositionRoot;
import libraryhub.LibrarySystem;
import libraryhub.TestCatalogs;
import libraryhub.event.EventType;
import libraryhub.event.ItemAvailability;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CirculationTest {

    private AdjustableClock clock;
    private LibrarySystem system;

    @BeforeEach
    void setUp() {
        clock = new AdjustableClock(Instant.parse("2026-10-01T10:00:00Z"), ZoneOffset.UTC);
        system = CompositionRoot.build(AppConfig.defaults(), TestCatalogs.catalog(), clock);
    }

    @Test
    void checkOutTakesACopyAndPublishesAnEvent() {
        List<ItemAvailability> seen = new ArrayList<>();
        system.desk().subscribe(EventType.ITEM_CHECKED_OUT, seen::add);

        var loan = system.desk().checkOut("b1", "m1").orElseThrow();

        assertEquals(2, system.inventory().available("b1"));
        assertEquals(LocalDate.of(2026, 10, 15), loan.dueDate());
        assertEquals(1, seen.size());
        assertEquals("sh-1", seen.get(0).shelfId());
        assertEquals(2, seen.get(0).copiesAvailable());
    }

    @Test
    void checkOutOfTheLastCopyThenNothingIsLeft() {
        assertTrue(system.desk().checkOut("a1", "m1").isPresent());

        assertTrue(system.desk().checkOut("a1", "m2").isEmpty()); // only 1 copy exists
        assertEquals(0, system.inventory().available("a1"));
    }

    @Test
    void returnPutsTheCopyBackAndClosesTheLoan() {
        var loan = system.desk().checkOut("b1", "m1").orElseThrow();

        assertTrue(system.desk().returnItem(loan.id()));
        assertFalse(system.desk().returnItem(loan.id())); // second return of the same loan is ignored

        assertEquals(3, system.inventory().available("b1"));
        assertEquals(0, system.ledger().openCount());
    }

    @Test
    void unknownItemIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> system.desk().checkOut("nope", "m1"));
    }

    @Test
    void scannerPublishesOnlyLoansThatArePastDue() {
        system.desk().checkOut("b1", "m1");
        clock.advance(Duration.ofDays(14)); // due today: not overdue yet
        assertEquals(0, system.scanner().scan());

        clock.advance(Duration.ofDays(2));
        assertEquals(1, system.scanner().scan());
        assertEquals(1, system.statistics().count(EventType.LOAN_OVERDUE));
    }
}
