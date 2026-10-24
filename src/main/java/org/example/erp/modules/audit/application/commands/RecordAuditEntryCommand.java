package org.example.erp.modules.audit.application.commands;

import java.time.Instant;
import java.util.Map;

public record RecordAuditEntryCommand(Instant occurredAt, String actor, String action, Map<String, Object> payload) {}
