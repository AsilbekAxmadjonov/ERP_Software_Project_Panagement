package org.example.erp.modules.audit.application.dto;

import java.time.Instant;
import java.util.Map;

public record AuditEntryDTO(long id, Instant occurredAt, String actor, String action, String summary, String subject,
                            Map<String, Object> payload) {}
