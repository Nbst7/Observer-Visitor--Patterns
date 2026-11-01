package libraryhub.concurrent;

import java.util.List;
import libraryhub.circulation.CirculationDesk;

/**
 * One producer thread's workload: {@code pairs} times it borrows an item and brings it back,
 * which publishes exactly two events per pair when stock is available.
 */
public final class CirculationProducer implements Runnable {

    private final CirculationDesk desk;
    private final List<String> itemIds;
    private final int pairs;
    private final String memberId;

    public CirculationProducer(CirculationDesk desk, List<String> itemIds, int pairs, String memberId) {
        this.desk = desk;
        this.itemIds = List.copyOf(itemIds);
        this.pairs = pairs;
        this.memberId = memberId;
    }

    @Override
    public void run() {
        for (int i = 0; i < pairs; i++) {
            String itemId = itemIds.get(i % itemIds.size());
            desk.checkOut(itemId, memberId).ifPresent(loan -> desk.returnItem(loan.id()));
        }
    }
}
