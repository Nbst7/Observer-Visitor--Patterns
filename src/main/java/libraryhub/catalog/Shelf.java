package libraryhub.catalog;

import java.util.List;
import libraryhub.visitor.Visitor;

public record Shelf(String id, String label, List<Item> items) implements Element {

    public Shelf {
        id = Checks.text(id, "id");
        label = Checks.text(label, "label");
        items = List.copyOf(items);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitShelf(this);
    }
}
