package org.example.erp.modules.expenses.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.erp.modules.expenses.domain.CategoryKind;
import org.example.erp.modules.expenses.domain.EntryStatus;
import org.example.erp.modules.expenses.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses_entries")
@Getter @Setter
class CashEntryRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private CategoryKind kind;
    private long categoryId;
    private String categoryName;
    private String accountCode;
    @Column(nullable = false)
    private LocalDate entryDate;
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(length = 16)
    private PaymentMethod method;
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private EntryStatus status;
    private LocalDate cancelledOn;
    private String createdBy;
}
