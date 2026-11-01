package libraryhub.catalog;

import libraryhub.visitor.Visitor;

public record PrintedBook(String id, String title, String author, int pages, int copies, double unitPrice)
        implements Item {

    public PrintedBook {
        id = Checks.text(id, "id");
        title = Checks.text(title, "title");
        author = Checks.text(author, "author");
        pages = Checks.nonNegative(pages, "pages");
        copies = Checks.nonNegative(copies, "copies");
        unitPrice = Checks.nonNegative(unitPrice, "unitPrice");
    }
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitPrintedBook(this);
    }
}
