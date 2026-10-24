package org.example.erp.modules.audit.application.queries;

import java.time.Instant;

/** All criteria are optional; {@code to} is exclusive and a null limit means the default page size. */
public record AuditQuery(Instant from, Instant to, String actor, String action, Integer limit) {}
