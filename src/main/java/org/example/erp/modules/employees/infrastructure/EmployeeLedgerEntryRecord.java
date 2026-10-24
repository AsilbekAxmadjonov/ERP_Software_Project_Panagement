package org.example.erp.modules.employees.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.erp.modules.employees.domain.LedgerEntryType;
import org.example.erp.modules.employees.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employees_ledger_entries", indexes = @Index(columnList = "employeeId"))
@Getter @Setter
class EmployeeLedgerEntryRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long employeeId;
    @Column(nullable = false)
    private LocalDate entryDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32)
    private LedgerEntryType type;
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(length = 16)
    private PaymentMethod method;
    /** yyyy-MM, salary accruals only. */
    @Column(name = "payroll_period", length = 7)
    private String period;
    private String description;
    private String createdBy;
}
