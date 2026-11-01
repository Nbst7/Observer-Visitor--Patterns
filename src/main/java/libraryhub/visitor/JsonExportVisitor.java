package libraryhub.visitor;

import java.util.List;
import java.util.stream.Collectors;
import libraryhub.catalog.AudioBook;
import libraryhub.catalog.EBook;
import libraryhub.catalog.Journal;
import libraryhub.catalog.Library;
import libraryhub.catalog.PrintedBook;
import libraryhub.catalog.Section;
import libraryhub.catalog.Shelf;

/**
 * Hand-written compact JSON export (no libraries). Each visit returns the JSON of its own
 * subtree and the parent embeds it, so nesting is correct by construction.
 */
public final class JsonExportVisitor implements Visitor<String> {

    @Override
    public String visitLibrary(Library library) {
        return object("library",
                text("name", library.name()),
                array("sections", library.sections().stream().map(s -> s.accept(this)).toList()));
    }

    @Override
    public String visitSection(Section section) {
        return object("section",
                text("id", section.id()),
                text("name", section.name()),
                array("shelves", section.shelves().stream().map(s -> s.accept(this)).toList()));
    }

    @Override
    public String visitShelf(Shelf shelf) {
        return object("shelf",
                text("id", shelf.id()),
                text("label", shelf.label()),
                array("items", shelf.items().stream().map(i -> i.accept(this)).toList()));
    }

    @Override
    public String visitPrintedBook(PrintedBook book) {
        return object("printedBook",
                text("id", book.id()), text("title", book.title()), text("author", book.author()),
                number("pages", book.pages()), number("copies", book.copies()),
                number("unitPrice", book.unitPrice()));
    }

    @Override
    public String visitJournal(Journal journal) {
        return object("journal",
                text("id", journal.id()), text("title", journal.title()),
                number("volume", journal.volume()), number("issue", journal.issue()),
                number("pages", journal.pages()), number("copies", journal.copies()),
                number("unitPrice", journal.unitPrice()));
    }

    @Override
    public String visitEBook(EBook eBook) {
        return object("ebook",
                text("id", eBook.id()), text("title", eBook.title()), text("author", eBook.author()),
                number("pages", eBook.pages()), number("fileSizeMb", eBook.fileSizeMb()),
                number("copies", eBook.copies()), number("unitPrice", eBook.unitPrice()));
    }

    @Override
    public String visitAudioBook(AudioBook audioBook) {
        return object("audioBook",
                text("id", audioBook.id()), text("title", audioBook.title()), text("author", audioBook.author()),
                number("durationMinutes", audioBook.durationMinutes()), number("copies", audioBook.copies()),
                number("unitPrice", audioBook.unitPrice()));
    }

    private static String object(String type, String... members) {
        StringBuilder json = new StringBuilder("{").append(text("type", type));
        for (String member : members) {
            json.append(',').append(member);
        }
        return json.append('}').toString();
    }

    private static String text(String key, String value) {
        return quote(key) + ":" + quote(value);
    }

    private static String number(String key, Number value) {
        return quote(key) + ":" + value;
    }

    private static String array(String key, List<String> renderedElements) {
        return quote(key) + ":[" + renderedElements.stream().collect(Collectors.joining(",")) + "]";
    }

    private static String quote(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
