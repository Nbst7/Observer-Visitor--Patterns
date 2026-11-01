package libraryhub.catalog;

import libraryhub.visitor.Visitor;

public record EBook(String id, String title, String author, int pages, double fileSizeMb, int copies,
                    double unitPrice) implements Item {

    public EBook {
        id = Checks.text(id, "id");
        title = Checks.text(title, "title");
        author = Checks.text(author, "author");
        pages = Checks.nonNegative(pages, "pages");
        fileSizeMb = Checks.nonNegative(fileSizeMb, "fileSizeMb");
        copies = Checks.nonNegative(copies, "copies");
        unitPrice = Checks.nonNegative(unitPrice, "unitPrice");
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitEBook(this);
    }
}
