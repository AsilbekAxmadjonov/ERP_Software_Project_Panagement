package org.example.erp.modules.audit.domain;

import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/** An immutable record of something that happened in the system; entries are only ever appended. */
@Getter
public final class AuditEntry {
    static final int MAX_ACTOR = 100;
    static final int MAX_ACTION = 120;
    static final int MAX_SUMMARY = 500;
    static final int MAX_SUBJECT = 200;

    private final Long id;
    private final Instant occurredAt;
    private final String actor;
    private final String action;
    private final String summary;
    private final String subject;
    private final Map<String, Object> payload;

    private AuditEntry(Long id, Instant occurredAt, String actor, String action, String summary, String subject,
                       Map<String, Object> payload) {
        this.id = id;
        this.occurredAt = occurredAt;
        this.actor = actor;
        this.action = action;
        this.summary = summary;
        this.subject = subject;
        this.payload = payload;
    }

    /** Derives the subject and summary from the action and payload, and strips anything that looks like a secret. */
    public static AuditEntry record(Instant occurredAt, String actor, String action, Map<String, Object> payload) {
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        if (action == null || action.isBlank()) throw new IllegalArgumentException("action is required");
        Map<String, Object> safe = SecretRedaction.redact(payload);
        String subject = AuditDescription.subjectOf(safe);
        return new AuditEntry(null, occurredAt, clip(actor, MAX_ACTOR), clip(action, MAX_ACTION),
                clip(AuditDescription.summarize(action, subject), MAX_SUMMARY), clip(subject, MAX_SUBJECT), safe);
    }

    public static AuditEntry restore(Long id, Instant occurredAt, String actor, String action, String summary,
                                     String subject, Map<String, Object> payload) {
        return new AuditEntry(id, occurredAt, actor, action, summary, subject, payload);
    }

    private static String clip(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
