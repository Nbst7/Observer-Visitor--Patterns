package libraryhub.visitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import libraryhub.TestCatalogs;
import libraryhub.catalog.Catalog;
import org.junit.jupiter.api.Test;

class ConsumptionTimeVisitorTest {

    private final Catalog catalog = TestCatalogs.catalog();

    @Test
    void sumsReadingAndListeningHoursOfOneCopyPerTitle() {
        // 60 pages/hour: b1 300/60 = 5, j1 80/60 = 4/3, e1 120/60 = 2,
        //                a1 600 min = 10, b2 200/60 = 10/3   -> 5 + 4/3 + 2 + 10 + 10/3 = 65/3
        double hours = catalog.library().accept(new ConsumptionTimeVisitor(60));

        assertEquals(65.0 / 3.0, hours, 1e-9);
    }

    @Test
    void worksOnASubtree() {
        double hours = catalog.shelf("sh-3").orElseThrow().accept(new ConsumptionTimeVisitor(60));

        assertEquals(10.0 + 10.0 / 3.0, hours, 1e-9);
    }

    @Test
    void rejectsNonPositiveSpeed() {
        assertThrows(IllegalArgumentException.class, () -> new ConsumptionTimeVisitor(0));
    }
}
