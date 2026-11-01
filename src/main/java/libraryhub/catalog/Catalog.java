package libraryhub.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Read-only index over a {@link Library} tree. Built once, never modified afterwards,
 * so it can be read from any thread without synchronization.
 */
public final class Catalog {

    private final Library library;
    private final Map<String, Shelf> shelves;
    private final Map<String, Location> locations;
    private final List<Item> items;
    private final List<String> sectionIds;

    public Catalog(Library library) {
        this.library = library;
        Map<String, Shelf> shelfIndex = new HashMap<>();
        Map<String, Location> locationIndex = new HashMap<>();
        List<Item> itemList = new ArrayList<>();
        List<String> sectionIdList = new ArrayList<>();
        for (Section section : library.sections()) {
            sectionIdList.add(section.id());
            for (Shelf shelf : section.shelves()) {
                if (shelfIndex.put(shelf.id(), shelf) != null) {
                    throw new IllegalArgumentException("Duplicate shelf id: " + shelf.id());
                }
                for (Item item : shelf.items()) {
                    if (locationIndex.put(item.id(), new Location(section.id(), shelf.id())) != null) {
                        throw new IllegalArgumentException("Duplicate item id: " + item.id());
                    }
                    itemList.add(item);
                }
            }
        }
        this.shelves = Collections.unmodifiableMap(shelfIndex);
        this.locations = Collections.unmodifiableMap(locationIndex);
        this.items = List.copyOf(itemList);
        this.sectionIds = List.copyOf(sectionIdList);
    }

    public Library library() {
        return library;
    }

    public Optional<Shelf> shelf(String shelfId) {
        return Optional.ofNullable(shelves.get(shelfId));
    }

    public Optional<Location> locate(String itemId) {
        return Optional.ofNullable(locations.get(itemId));
    }

    public List<Item> items() {
        return items;
    }

    public List<String> sectionIds() {
        return sectionIds;
    }
}
