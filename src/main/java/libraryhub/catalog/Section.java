package libraryhub.catalog;

import java.util.List;
import libraryhub.visitor.Visitor;

public record Section(String id, String name, List<Shelf> shelves) implements Element {

    public Section {
        id = Checks.text(id, "id");
        name = Checks.text(name, "name");
        shelves = List.copyOf(shelves);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitSection(this);
    }
}
