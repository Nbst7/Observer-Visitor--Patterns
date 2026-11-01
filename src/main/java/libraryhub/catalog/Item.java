package libraryhub.catalog;

public interface Item extends Element {
    String id();

    String title();
    int copies();
    double unitPrice();
}
