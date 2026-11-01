package libraryhub.catalog;

import libraryhub.visitor.Visitor;

public record DvdDisc(String id, String title, String director, int runtimeMinutes, int copies, double unitPrice)
        implements Item {

    public DvdDisc {
        id = Checks.text(id, "id");
        title = Checks.text(title, "title");
        director = Checks.text(director, "director");
        runtimeMinutes = Checks.nonNegative(runtimeMinutes, "runtimeMinutes");
        copies = Checks.nonNegative(copies, "copies");
        unitPrice = Checks.nonNegative(unitPrice, "unitPrice");
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitDvdDisc(this);
    }
}
