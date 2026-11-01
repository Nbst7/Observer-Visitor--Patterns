package libraryhub.catalog;

/** A leaf of the tree: something a member can borrow. */
public interface Item extends Element {
    String id();

    String title();

    /** Total number of copies (licenses for e-books) owned by the library. */
    int copies();

    /** Price of one copy. */
    double unitPrice();
}
