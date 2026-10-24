package org.example.erp.modules.accounting.application.events;

import org.example.erp.platform.BaseIntegrationEvent;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** The lock date moved: forward when a period is closed, back (or removed, lockDate null) when it is reopened. */
public final class PeriodLockChanged extends BaseIntegrationEvent {
    private final boolean closed;
    private final LocalDate previousLockDate;
    private final LocalDate lockDate;

    private PeriodLockChanged(boolean closed, LocalDate previousLockDate, LocalDate lockDate) {
        super(null, null);
        this.closed = closed;
        this.previousLockDate = previousLockDate;
        this.lockDate = lockDate;
    }

    public static PeriodLockChanged closed(LocalDate previousLockDate, LocalDate lockDate) {
        return new PeriodLockChanged(true, previousLockDate, lockDate);
    }

    public static PeriodLockChanged reopened(LocalDate previousLockDate, LocalDate lockDate) {
        return new PeriodLockChanged(false, previousLockDate, lockDate);
    }

    public LocalDate lockDate() {
        return lockDate;
    }

    @Override
    public String eventName() {
        return closed ? "accounting.period-closed" : "accounting.period-reopened";
    }

    @Override
    public Map<String, Object> payload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("lockDate", lockDate == null ? null : lockDate.toString());
        payload.put("previousLockDate", previousLockDate == null ? null : previousLockDate.toString());
        return payload;
    }
}
