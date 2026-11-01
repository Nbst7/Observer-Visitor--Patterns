package libraryhub.circulation;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

/** A clock the demo and the tests can move forward, e.g. to make loans overdue. */
public final class AdjustableClock extends Clock {

    private final AtomicReference<Instant> now;
    private final ZoneId zone;

    public AdjustableClock(Instant start, ZoneId zone) {
        this.now = new AtomicReference<>(start);
        this.zone = zone;
    }

    public void advance(Duration amount) {
        now.updateAndGet(current -> current.plus(amount));
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId newZone) {
        return new AdjustableClock(now.get(), newZone);
    }

    @Override
    public Instant instant() {
        return now.get();
    }
}
