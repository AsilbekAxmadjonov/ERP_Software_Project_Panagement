package org.example.erp.platform;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Transport-neutral envelope; payload keys are stable camelCase and contain only scalars. */
public interface IntegrationEvent {
    UUID eventId();

    String eventName();

    int schemaVersion();

    Instant occurredAt();

    String correlationId();

    String causationId();

    Map<String, Object> payload();

    /**
     * Who caused the event when that is not the authenticated caller, e.g. the username of a login attempt.
     * Null (the default) means the current actor.
     */
    default String actor() {
        return null;
    }
}
