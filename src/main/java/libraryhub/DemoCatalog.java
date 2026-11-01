package libraryhub;

import java.util.List;
import libraryhub.catalog.AudioBook;
import libraryhub.catalog.EBook;
import libraryhub.catalog.Journal;
import libraryhub.catalog.Library;
import libraryhub.catalog.PrintedBook;
import libraryhub.catalog.Section;
import libraryhub.catalog.Shelf;

/** Sample data for the demo. Prices and page counts are illustrative. */
public final class DemoCatalog {

    private DemoCatalog() {
    }

    public static Library create() {
        return new Library("Astana IT University Library", List.of(
                new Section("sec-cs", "Computer Science", List.of(
                        new Shelf("sh-cs-1", "Algorithms", List.of(
                                new PrintedBook("bk-clrs", "Introduction to Algorithms", "Cormen", 1312, 6, 45.0),
                                new PrintedBook("bk-clean", "Clean Code", "Martin", 464, 5, 30.0),
                                new Journal("jr-cacm", "Communications of the ACM", 69, 10, 120, 4, 12.0))),
                        new Shelf("sh-cs-2", "Design", List.of(
                                new PrintedBook("bk-gof", "Design Patterns", "Gamma", 395, 1, 40.0),
                                new EBook("eb-hf", "Head First Design Patterns", "Freeman", 694, 38.5, 8, 25.0),
                                new AudioBook("au-pragmatic", "The Pragmatic Programmer", "Hunt", 570, 5, 20.0))))),
                new Section("sec-lit", "Literature", List.of(
                        new Shelf("sh-lit-1", "Classics", List.of(
                                new PrintedBook("bk-abai", "Abai Zholy", "Auezov", 640, 7, 14.0),
                                new AudioBook("au-1984", "1984", "Orwell", 660, 6, 15.0),
                                new EBook("eb-prince", "The Little Prince", "Saint-Exupery", 96, 2.1, 10, 5.0)))))));
    }
}
