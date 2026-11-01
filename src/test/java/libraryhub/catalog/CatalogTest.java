package libraryhub.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import libraryhub.TestCatalogs;
import org.junit.jupiter.api.Test;

class CatalogTest {

    @Test
    void locatesItemsAndShelves() {
        Catalog catalog = TestCatalogs.catalog();

        assertEquals(new Location("sec-art", "sh-3"), catalog.locate("b2").orElseThrow());
        assertEquals("A2", catalog.shelf("sh-2").orElseThrow().label());
        assertTrue(catalog.locate("missing").isEmpty());
        assertEquals(5, catalog.items().size());
        assertEquals(List.of("sec-sci", "sec-art"), catalog.sectionIds());
    }

    @Test
    void rejectsDuplicateItemIds() {
        Library library = new Library("L", List.of(new Section("s", "S", List.of(
                new Shelf("sh", "A", List.of(
                        new PrintedBook("dup", "One", "A", 1, 1, 1.0),
                        new PrintedBook("dup", "Two", "B", 1, 1, 1.0)))))));

        assertThrows(IllegalArgumentException.class, () -> new Catalog(library));
    }

    @Test
    void elementsRejectInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> new PrintedBook("b", "T", "A", 1, -1, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new Shelf(" ", "label", List.of()));
    }
}
