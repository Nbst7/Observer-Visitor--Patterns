package libraryhub.catalog;

import libraryhub.visitor.Visitor;

/** A node of the catalog tree. Elements hold data only; operations live in visitors. */
public interface Element {
    <R> R accept(Visitor<R> visitor);
}
