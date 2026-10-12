package org.example.erp.platform;

import java.time.Instant;
import java.util.UUID;

/** Carries the envelope metadata so concrete events only declare name, version and payload. */
public abstract class BaseIntegrationEvent implements IntegrationEvent {
    private final UUID eventId = UUID.randomUUID();
    private final Instant occurredAt = Instant.now();
    private final String correlationId;
    private final String causationId;

    protected BaseIntegrationEvent(String correlationId, String causationId) {
        this.correlationId = correlationId;
        this.causationId = causationId;
    }

    @Override
    public UUID eventId() {
        return eventId;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }

    @Override
    public String correlationId() {
        return correlationId;
    }

    @Override
    public String causationId() {
        return causationId;
    }

    @Override
    public int schemaVersion() {
        return 1;
    }
}
