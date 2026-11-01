package libraryhub.catalog;

import libraryhub.visitor.Visitor;

public record AudioBook(String id, String title, String author, int durationMinutes, int copies, double unitPrice)
        implements Item {

    public AudioBook {
        id = Checks.text(id, "id");
        title = Checks.text(title, "title");
        author = Checks.text(author, "author");
        durationMinutes = Checks.nonNegative(durationMinutes, "durationMinutes");
        copies = Checks.nonNegative(copies, "copies");
        unitPrice = Checks.nonNegative(unitPrice, "unitPrice");
    }
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitAudioBook(this);
    }
}
