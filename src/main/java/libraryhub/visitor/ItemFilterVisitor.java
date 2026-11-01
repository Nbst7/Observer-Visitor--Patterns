package libraryhub.visitor;

import java.util.List;
import java.util.function.Predicate;
import libraryhub.catalog.AudioBook;
import libraryhub.catalog.EBook;
import libraryhub.catalog.Item;
import libraryhub.catalog.Journal;
import libraryhub.catalog.Library;
import libraryhub.catalog.PrintedBook;
import libraryhub.catalog.Section;
import libraryhub.catalog.Shelf;

/** Filter: returns the items that match the predicate, in tree order. */
public final class ItemFilterVisitor implements Visitor<List<Item>> {

    private final Predicate<Item> predicate;

    public ItemFilterVisitor(Predicate<Item> predicate) {
        this.predicate = predicate;
    }

    @Override
    public List<Item> visitLibrary(Library library) {
        return library.sections().stream().flatMap(section -> section.accept(this).stream()).toList();
    }

    @Override
    public List<Item> visitSection(Section section) {
        return section.shelves().stream().flatMap(shelf -> shelf.accept(this).stream()).toList();
    }

    @Override
    public List<Item> visitShelf(Shelf shelf) {
        return shelf.items().stream().flatMap(item -> item.accept(this).stream()).toList();
    }

    @Override
    public List<Item> visitPrintedBook(PrintedBook book) {
        return matching(book);
    }

    @Override
    public List<Item> visitJournal(Journal journal) {
        return matching(journal);
    }

    @Override
    public List<Item> visitEBook(EBook eBook) {
        return matching(eBook);
    }

    @Override
    public List<Item> visitAudioBook(AudioBook audioBook) {
        return matching(audioBook);
    }

    private List<Item> matching(Item item) {
        return predicate.test(item) ? List.of(item) : List.of();
    }
}
