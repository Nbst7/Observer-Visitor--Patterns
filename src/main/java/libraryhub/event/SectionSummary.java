package libraryhub.event;

import java.util.Objects;

/** Aggregated circulation data of one section; republished by the section aggregator. */
public record SectionSummary(String sectionId, long checkouts, long returns) implements Event {

    public SectionSummary {
        Objects.requireNonNull(sectionId, "sectionId");
    }

    /** Total number of availability events counted for the section. */
    public long updates() {
        return checkouts + returns;
    }

    /** Copies currently out of the section because of the counted events. */
    public long copiesOut() {
        return checkouts - returns;
    }

    @Override
    public EventType type() {
        return EventType.SECTION_SUMMARY;
    }
}
