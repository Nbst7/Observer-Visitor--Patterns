package libraryhub.catalog;

import libraryhub.visitor.Visitor;

public interface Element {
    <R> R accept(Visitor<R> visitor);
}
