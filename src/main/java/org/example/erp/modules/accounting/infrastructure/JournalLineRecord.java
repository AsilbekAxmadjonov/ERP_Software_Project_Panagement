package org.example.erp.modules.accounting.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "accounting_journal_lines")
@Getter @Setter
class JournalLineRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private JournalEntryRecord entry;
    @Column(nullable = false)
    private String accountCode;
    private Long partnerId;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal debit;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal credit;
    private String memo;
}
