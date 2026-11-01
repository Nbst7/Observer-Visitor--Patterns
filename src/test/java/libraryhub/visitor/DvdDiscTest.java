package libraryhub.visitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import libraryhub.catalog.DvdDisc;
import libraryhub.catalog.Item;
import libraryhub.catalog.PrintedBook;
import libraryhub.catalog.Shelf;
import org.junit.jupiter.api.Test;

class DvdDiscTest {

    private final Shelf shelf = new Shelf("sh-dvd", "Media", List.of(
            new DvdDisc("d1", "Algorithms Lecture", "Knuth", 90, 2, 12.0),
            new PrintedBook("b9", "Notes", "Anon", 60, 1, 8.0)));

    @Test
    void costTreatsADiscAsAPhysicalCopy() {
        // d1: (12 + 2) * 2 = 28, b9: (8 + 2) * 1 = 10
        assertEquals(38.0, shelf.accept(new ReplacementCostVisitor(2.0)), 1e-9);
    }

    @Test
    void summaryCountsDiscsAsTheirOwnKind() {
        InventorySummary summary = shelf.accept(new InventorySummaryVisitor());

        assertEquals(2, summary.titles());
        assertEquals(3, summary.copies());
        assertEquals(2, summary.copiesByKind().get(ItemKind.DVD));
        assertEquals(1, summary.copiesByKind().get(ItemKind.PRINTED_BOOK));
    }

    @Test
    void filterFindsTheDisc() {
        List<Item> found = shelf.accept(new ItemFilterVisitor(item -> item instanceof DvdDisc));

        assertEquals(List.of("d1"), found.stream().map(Item::id).toList());
    }

    @Test
    void jsonContainsTheDiscWithItsOwnFields() {
        String json = shelf.accept(new JsonExportVisitor());

        assertEquals("{\"type\":\"shelf\",\"id\":\"sh-dvd\",\"label\":\"Media\",\"items\":["
                + "{\"type\":\"dvdDisc\",\"id\":\"d1\",\"title\":\"Algorithms Lecture\",\"director\":\"Knuth\","
                + "\"runtimeMinutes\":90,\"copies\":2,\"unitPrice\":12.0},"
                + "{\"type\":\"printedBook\",\"id\":\"b9\",\"title\":\"Notes\",\"author\":\"Anon\","
                + "\"pages\":60,\"copies\":1,\"unitPrice\":8.0}]}", json);
    }

    @Test
    void consumptionTimeUsesTheRuntime() {
        // d1: 90 min = 1.5 h, b9: 60 pages / 60 = 1 h
        assertEquals(2.5, shelf.accept(new ConsumptionTimeVisitor(60)), 1e-9);
    }
}
