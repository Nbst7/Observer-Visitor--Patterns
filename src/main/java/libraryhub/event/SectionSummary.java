package libraryhub.event;

import java.util.Objects;

public record SectionSummary(String sectionId, long checkouts, long returns) implements Event {

    public SectionSummary {
        Objects.requireNonNull(sectionId, "sectionId");
    }

    public long updates() {
        return checkouts + returns;
    }

    public long copiesOut() {
        return checkouts - returns;
    }

    @Override
    public EventType type() {
        return EventType.SECTION_SUMMARY;
    }
}
