package libraryhub.event;

/** Listeners subscribe per event type. */
public enum EventType {
    ITEM_CHECKED_OUT,
    ITEM_RETURNED,
    LOAN_OVERDUE,
    SECTION_SUMMARY,
    INSPECTION_COMPLETED
}
