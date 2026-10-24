package org.example.erp.modules.accounting.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "accounting_period_lock")
@Getter @Setter
class PeriodLockRecord {
    @Id
    private Long id;
    private LocalDate lockDate;
    private String changedBy;
    private Instant changedAt;
}
