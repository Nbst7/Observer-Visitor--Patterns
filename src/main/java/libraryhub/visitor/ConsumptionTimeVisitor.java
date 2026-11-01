package libraryhub.visitor;

import libraryhub.catalog.AudioBook;
import libraryhub.catalog.EBook;
import libraryhub.catalog.Journal;
import libraryhub.catalog.Library;
import libraryhub.catalog.PrintedBook;
import libraryhub.catalog.Section;
import libraryhub.catalog.Shelf;

/**
 * Hours needed to read or listen to one copy of every title. Text items are measured in pages
 * at a given reading speed, audio books by their duration.
 */
public final class ConsumptionTimeVisitor implements Visitor<Double> {

    private final double pagesPerHour;

    public ConsumptionTimeVisitor(double pagesPerHour) {
        if (pagesPerHour <= 0) {
            throw new IllegalArgumentException("pagesPerHour must be positive");
        }
        this.pagesPerHour = pagesPerHour;
    }

    @Override
    public Double visitLibrary(Library library) {
        return library.sections().stream().mapToDouble(section -> section.accept(this)).sum();
    }

    @Override
    public Double visitSection(Section section) {
        return section.shelves().stream().mapToDouble(shelf -> shelf.accept(this)).sum();
    }

    @Override
    public Double visitShelf(Shelf shelf) {
        return shelf.items().stream().mapToDouble(item -> item.accept(this)).sum();
    }

    @Override
    public Double visitPrintedBook(PrintedBook book) {
        return book.pages() / pagesPerHour;
    }

    @Override
    public Double visitJournal(Journal journal) {
        return journal.pages() / pagesPerHour;
    }

    @Override
    public Double visitEBook(EBook eBook) {
        return eBook.pages() / pagesPerHour;
    }

    @Override
    public Double visitAudioBook(AudioBook audioBook) {
        return audioBook.durationMinutes() / 60.0;
    }
}
