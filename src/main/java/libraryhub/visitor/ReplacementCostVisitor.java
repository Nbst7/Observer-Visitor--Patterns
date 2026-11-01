package libraryhub.visitor;

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
 * Numeric calculation: what it would cost to buy every copy again.
 * A physical copy costs its price plus shipping, an e-book license costs only its price.
 */
public final class ReplacementCostVisitor implements Visitor<Double> {

    private final double shippingPerPhysicalCopy;

    public ReplacementCostVisitor(double shippingPerPhysicalCopy) {
        this.shippingPerPhysicalCopy = shippingPerPhysicalCopy;
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
        return physicalCost(book);
    }

    @Override
    public Double visitJournal(Journal journal) {
        return physicalCost(journal);
    }

    @Override
    public Double visitEBook(EBook eBook) {
        return eBook.unitPrice() * eBook.copies();
    }

    @Override
    public Double visitAudioBook(AudioBook audioBook) {
        return physicalCost(audioBook);
    }

    @Override
    public Double visitDvdDisc(DvdDisc dvdDisc) {
        return physicalCost(dvdDisc);
    }

    private double physicalCost(Item item) {
        return (item.unitPrice() + shippingPerPhysicalCopy) * item.copies();
    }
}
