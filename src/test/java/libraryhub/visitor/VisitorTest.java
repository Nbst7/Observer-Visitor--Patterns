package libraryhub.visitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import libraryhub.TestCatalogs;
import libraryhub.catalog.Catalog;
import libraryhub.catalog.Item;
import libraryhub.catalog.Library;
import libraryhub.catalog.PrintedBook;
import libraryhub.catalog.Section;
import libraryhub.catalog.Shelf;
import org.junit.jupiter.api.Test;

/** Expected values are calculated by hand from the tree documented in {@link TestCatalogs}. */
class VisitorTest {

    private final Catalog catalog = TestCatalogs.catalog();

    @Test
    void replacementCostOfTheWholeLibrary() {
        // sh-1: b1 (10+2)*3 = 36, j1 (5+2)*2 = 14                       -> 50
        // sh-2: e1 8*5 = 40 (e-book, no shipping)                       -> 40
        // sh-3: a1 (20+2)*1 = 22, b2 (15+2)*4 = 68                      -> 90
        double total = catalog.library().accept(new ReplacementCostVisitor(2.0));

        assertEquals(180.0, total, 1e-9);
    }

    @Test
    void replacementCostOfASingleShelfAndSection() {
        assertEquals(50.0, catalog.shelf("sh-1").orElseThrow().accept(new ReplacementCostVisitor(2.0)), 1e-9);
        Section science = catalog.library().sections().get(0);
        assertEquals(90.0, science.accept(new ReplacementCostVisitor(2.0)), 1e-9);
    }

    @Test
    void inventorySummaryAccumulatesAcrossTheTree() {
        InventorySummary summary = catalog.library().accept(new InventorySummaryVisitor());

        assertEquals(5, summary.titles());
        assertEquals(15, summary.copies()); // 3 + 2 + 5 + 1 + 4
        assertEquals(7, summary.copiesByKind().get(ItemKind.PRINTED_BOOK)); // 3 + 4
        assertEquals(2, summary.copiesByKind().get(ItemKind.JOURNAL));
        assertEquals(5, summary.copiesByKind().get(ItemKind.E_BOOK));
        assertEquals(1, summary.copiesByKind().get(ItemKind.AUDIO_BOOK));
    }

    @Test
    void visitorOnASubtreeIgnoresTheRestOfTheTree() {
        InventorySummary shelfOnly = catalog.shelf("sh-3").orElseThrow().accept(new InventorySummaryVisitor());

        assertEquals(2, shelfOnly.titles());
        assertEquals(5, shelfOnly.copies()); // a1: 1, b2: 4
    }

    @Test
    void filterReturnsMatchingItemsInTreeOrder() {
        List<Item> bigStock = catalog.library().accept(new ItemFilterVisitor(item -> item.copies() >= 3));

        assertEquals(List.of("b1", "e1", "b2"), bigStock.stream().map(Item::id).toList());
    }

    @Test
    void filterWithNoMatchesReturnsEmptyList() {
        assertEquals(List.of(), catalog.library().accept(new ItemFilterVisitor(item -> item.copies() > 100)));
    }

    @Test
    void jsonOfASectionIsExactlyNested() {
        Section arts = catalog.library().sections().get(1);

        String expected = "{\"type\":\"section\",\"id\":\"sec-art\",\"name\":\"Arts\",\"shelves\":["
                + "{\"type\":\"shelf\",\"id\":\"sh-3\",\"label\":\"B1\",\"items\":["
                + "{\"type\":\"audioBook\",\"id\":\"a1\",\"title\":\"Art History\",\"author\":\"Gombrich\","
                + "\"durationMinutes\":600,\"copies\":1,\"unitPrice\":20.0},"
                + "{\"type\":\"printedBook\",\"id\":\"b2\",\"title\":\"Colors\",\"author\":\"Itten\","
                + "\"pages\":200,\"copies\":4,\"unitPrice\":15.0}]}]}";
        assertEquals(expected, arts.accept(new JsonExportVisitor()));
    }

    @Test
    void jsonOfTheWholeLibraryKeepsBracketsBalancedAndOrder() {
        String json = catalog.library().accept(new JsonExportVisitor());

        assertEquals(count(json, '{'), count(json, '}'));
        assertEquals(count(json, '['), count(json, ']'));
        assertEquals(1 + 2 + 3 + 5, count(json, '{')); // library, sections, shelves, items
        assertEquals(1 + 2 + 3, count(json, '[')); // sections, shelves, items arrays
        assertEquals(true, json.startsWith("{\"type\":\"library\",\"name\":\"Test Library\",\"sections\":["));
    }

    @Test
    void jsonEscapesQuotesBackslashesAndControlCharacters() {
        Library library = new Library("L", List.of(new Section("s", "S", List.of(new Shelf("sh", "A", List.of(
                new PrintedBook("x", "He said \"hi\"\\\n\tend\u0001", "A", 1, 1, 1.0)))))));

        String json = library.accept(new JsonExportVisitor());

        assertEquals(true, json.contains("\"title\":\"He said \\\"hi\\\"\\\\\\n\\tend\\u0001\""));
    }

    @Test
    void eachVisitorCallStartsFromItsOwnState() {
        InventorySummaryVisitor first = new InventorySummaryVisitor();
        catalog.library().accept(first);
        InventorySummary second = catalog.shelf("sh-1").orElseThrow().accept(new InventorySummaryVisitor());

        assertEquals(2, second.titles()); // a fresh visitor does not see the first traversal
    }

    private static long count(String text, char c) {
        return text.chars().filter(ch -> ch == c).count();
    }
}
