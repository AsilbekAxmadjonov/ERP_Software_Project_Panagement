package org.example.erp.modules.audit.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_entries", indexes = {
        @Index(name = "audit_entries_occurred_at_idx", columnList = "occurredAt"),
        @Index(name = "audit_entries_actor_idx", columnList = "actor"),
        @Index(name = "audit_entries_action_idx", columnList = "action")})
@Getter @Setter
class AuditEntryRecord {
    static final int MAX_PAYLOAD = 20000;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Instant occurredAt;
    @Column(length = 100)
    private String actor;
    @Column(nullable = false, length = 120)
    private String action;
    @Column(nullable = false, length = 500)
    private String summary;
    @Column(length = 200)
    private String subject;
    /** The redacted event payload as JSON text. */
    @Column(nullable = false, length = MAX_PAYLOAD)
    private String payload;
}
