package org.example.erp.modules.audit.domain;

import java.time.Instant;

/**
 * Every criterion is optional. {@code to} is exclusive. {@code action} matches the exact event name or a whole
 * prefix group, so "sales" matches "sales.invoice-issued" but not "salesforce.sync".
 */
public record AuditFilter(Instant from, Instant to, String actor, String action, int limit) {}
