package libraryhub.visitor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public record InventorySummary(int titles, int copies, Map<ItemKind, Integer> copiesByKind) {

    public InventorySummary {
        copiesByKind = Collections.unmodifiableMap(new EnumMap<>(copiesByKind));
    }
}
