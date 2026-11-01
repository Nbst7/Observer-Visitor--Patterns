package libraryhub.visitor;

import java.util.EnumMap;
import java.util.Map;
import libraryhub.catalog.AudioBook;
import libraryhub.catalog.DvdDisc;
import libraryhub.catalog.EBook;
import libraryhub.catalog.Item;
import libraryhub.catalog.Journal;
import libraryhub.catalog.Library;
import libraryhub.catalog.PrintedBook;
import libraryhub.catalog.Section;
import libraryhub.catalog.Shelf;

/**
 * State accumulated across the whole traversal: number of titles, copies and copies per kind.
 * The visitor is stateful, so use one instance per traversal. Every visit method returns the
 * snapshot at that moment; the result of the root visit is the total.
 */
public final class InventorySummaryVisitor implements Visitor<InventorySummary> {

    private final Map<ItemKind, Integer> copiesByKind = new EnumMap<>(ItemKind.class);
    private int titles;
    private int copies;

    public InventorySummaryVisitor() {
        for (ItemKind kind : ItemKind.values()) {
            copiesByKind.put(kind, 0);
        }
    }

    @Override
    public InventorySummary visitLibrary(Library library) {
        library.sections().forEach(section -> section.accept(this));
        return snapshot();
    }

    @Override
    public InventorySummary visitSection(Section section) {
        section.shelves().forEach(shelf -> shelf.accept(this));
        return snapshot();
    }

    @Override
    public InventorySummary visitShelf(Shelf shelf) {
        shelf.items().forEach(item -> item.accept(this));
        return snapshot();
    }

    @Override
    public InventorySummary visitPrintedBook(PrintedBook book) {
        return count(ItemKind.PRINTED_BOOK, book);
    }

    @Override
    public InventorySummary visitJournal(Journal journal) {
        return count(ItemKind.JOURNAL, journal);
    }

    @Override
    public InventorySummary visitEBook(EBook eBook) {
        return count(ItemKind.E_BOOK, eBook);
    }

    @Override
    public InventorySummary visitAudioBook(AudioBook audioBook) {
        return count(ItemKind.AUDIO_BOOK, audioBook);
    }

    @Override
    public InventorySummary visitDvdDisc(DvdDisc dvdDisc) {
        return count(ItemKind.DVD, dvdDisc);
    }

    private InventorySummary count(ItemKind kind, Item item) {
        titles++;
        copies += item.copies();
        copiesByKind.merge(kind, item.copies(), Integer::sum);
        return snapshot();
    }

    private InventorySummary snapshot() {
        return new InventorySummary(titles, copies, copiesByKind);
    }
}
