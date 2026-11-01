package libraryhub.circulation;

import java.util.List;
import libraryhub.catalog.Catalog;
import libraryhub.catalog.Item;
import libraryhub.catalog.Shelf;
import libraryhub.event.EventType;
import libraryhub.event.InspectionCompleted;
import libraryhub.event.ListenerRegistry;
import libraryhub.event.Observer;
import libraryhub.event.Subject;
import libraryhub.visitor.InventorySummary;
import libraryhub.visitor.InventorySummaryVisitor;
import libraryhub.visitor.ItemFilterVisitor;
import libraryhub.visitor.ReplacementCostVisitor;


public final class ShelfInspector implements Subject<InspectionCompleted> {

    private final Catalog catalog;
    private final Inventory inventory;
    private final ListenerRegistry<InspectionCompleted> listeners;
    private final int lowStockThreshold;
    private final double shippingPerCopy;

    public ShelfInspector(Catalog catalog, Inventory inventory, ListenerRegistry<InspectionCompleted> listeners,
                          int lowStockThreshold, double shippingPerCopy) {
        this.catalog = catalog;
        this.inventory = inventory;
        this.listeners = listeners;
        this.lowStockThreshold = lowStockThreshold;
        this.shippingPerCopy = shippingPerCopy;
    }

    public InspectionCompleted inspect(String shelfId, String triggerLoanId) {
        Shelf shelf = catalog.shelf(shelfId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown shelf: " + shelfId));
        // Stateful visitors are created per traversal, so concurrent inspections never share state.
        double cost = shelf.accept(new ReplacementCostVisitor(shippingPerCopy));
        InventorySummary summary = shelf.accept(new InventorySummaryVisitor());
        List<Item> lowStock = shelf.accept(new ItemFilterVisitor(item -> inventory.available(item.id()) <= lowStockThreshold));
        InspectionCompleted result = new InspectionCompleted(shelfId, triggerLoanId, summary.titles(),
                summary.copies(), lowStock.stream().map(Item::id).toList(), cost);
        listeners.publish(result);
        return result;
    }

    @Override
    public void subscribe(EventType type, Observer<? super InspectionCompleted> observer) {
        listeners.subscribe(type, observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<? super InspectionCompleted> observer) {
        listeners.unsubscribe(type, observer);
    }
}
