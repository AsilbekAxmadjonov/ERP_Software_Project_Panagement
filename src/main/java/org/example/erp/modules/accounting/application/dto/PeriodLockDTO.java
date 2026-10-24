package org.example.erp.modules.accounting.application.dto;

import java.time.Instant;
import java.time.LocalDate;

/** lockDate is null while no period is closed. */
public record PeriodLockDTO(LocalDate lockDate, String changedBy, Instant changedAt) {}
