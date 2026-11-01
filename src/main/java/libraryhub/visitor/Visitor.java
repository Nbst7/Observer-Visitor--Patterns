package libraryhub.visitor;

import libraryhub.catalog.AudioBook;
import libraryhub.catalog.DvdDisc;
import libraryhub.catalog.EBook;
import libraryhub.catalog.Journal;
import libraryhub.catalog.Library;
import libraryhub.catalog.PrintedBook;
import libraryhub.catalog.Section;
import libraryhub.catalog.Shelf;

/**
 * Double dispatch: {@code element.accept(visitor)} picks the element class, the element then
 * calls the matching method here. The visitor, not the tree, walks the children, so each
 * operation decides the order and what to do with the children's results.
 */
public interface Visitor<R> {

    R visitLibrary(Library library);

    R visitSection(Section section);

    R visitShelf(Shelf shelf);

    R visitPrintedBook(PrintedBook book);

    R visitJournal(Journal journal);

    R visitEBook(EBook eBook);

    R visitAudioBook(AudioBook audioBook);

    R visitDvdDisc(DvdDisc dvdDisc);
}
