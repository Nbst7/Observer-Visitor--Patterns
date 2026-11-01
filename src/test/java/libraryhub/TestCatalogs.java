package libraryhub;

import java.util.List;
import libraryhub.catalog.AudioBook;
import libraryhub.catalog.Catalog;
import libraryhub.catalog.EBook;
import libraryhub.catalog.Journal;
import libraryhub.catalog.Library;
import libraryhub.catalog.PrintedBook;
import libraryhub.catalog.Section;
import libraryhub.catalog.Shelf;

/**
 * Small tree whose results are calculated by hand in the tests.
 *
 * <pre>
 * Test Library
 *  +- sec-sci "Science"
 *  |   +- sh-1 "A1": b1 PrintedBook 3 x 10.0 (300 p), j1 Journal 2 x 5.0 (80 p)
 *  |   +- sh-2 "A2": e1 EBook 5 x 8.0 (120 p)
 *  +- sec-art "Arts"
 *      +- sh-3 "B1": a1 AudioBook 1 x 20.0 (600 min), b2 PrintedBook 4 x 15.0 (200 p)
 * </pre>
 */
public final class TestCatalogs {

    private TestCatalogs() {
    }

    public static Library library() {
        return new Library("Test Library", List.of(
                new Section("sec-sci", "Science", List.of(
                        new Shelf("sh-1", "A1", List.of(
                                new PrintedBook("b1", "Physics 101", "Feynman", 300, 3, 10.0),
                                new Journal("j1", "Nature Weekly", 12, 3, 80, 2, 5.0))),
                        new Shelf("sh-2", "A2", List.of(
                                new EBook("e1", "Quantum Notes", "Dirac", 120, 4.5, 5, 8.0))))),
                new Section("sec-art", "Arts", List.of(
                        new Shelf("sh-3", "B1", List.of(
                                new AudioBook("a1", "Art History", "Gombrich", 600, 1, 20.0),
                                new PrintedBook("b2", "Colors", "Itten", 200, 4, 15.0)))))));
    }

    public static Catalog catalog() {
        return new Catalog(library());
    }

    /** Same tree shape, but with plenty of copies so that producers never run out of stock. */
    public static Catalog catalogWithManyCopies() {
        return new Catalog(new Library("Big Stock", List.of(
                new Section("sec-sci", "Science", List.of(
                        new Shelf("sh-1", "A1", List.of(
                                new PrintedBook("b1", "Physics 101", "Feynman", 300, 50, 10.0),
                                new Journal("j1", "Nature Weekly", 12, 3, 80, 50, 5.0))))),
                new Section("sec-art", "Arts", List.of(
                        new Shelf("sh-3", "B1", List.of(
                                new PrintedBook("b2", "Colors", "Itten", 200, 50, 15.0))))))));
    }
}
