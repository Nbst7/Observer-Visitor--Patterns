package libraryhub.catalog;

import libraryhub.visitor.Visitor;

public record Journal(String id, String title, int volume, int issue, int pages, int copies, double unitPrice)
        implements Item {

    public Journal {
        id = Checks.text(id, "id");
        title = Checks.text(title, "title");
        volume = Checks.nonNegative(volume, "volume");
        issue = Checks.nonNegative(issue, "issue");
        pages = Checks.nonNegative(pages, "pages");
        copies = Checks.nonNegative(copies, "copies");
        unitPrice = Checks.nonNegative(unitPrice, "unitPrice");
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitJournal(this);
    }
}
