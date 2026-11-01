package libraryhub.catalog;

import java.util.List;
import libraryhub.visitor.Visitor;

public record Library(String name, List<Section> sections) implements Element {

    public Library {
        name = Checks.text(name, "name");
        sections = List.copyOf(sections);
    }
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitLibrary(this);
    }
}
