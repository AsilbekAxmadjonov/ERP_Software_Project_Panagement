package org.example.erp.modules.sales.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_credit_note_lines")
@Getter @Setter
class CreditNoteLineRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private CreditNoteRecord creditNote;
    private long productId;
    private String productName;
    private String unit;
    @Column(precision = 19, scale = 3)
    private BigDecimal quantity;
    @Column(precision = 19, scale = 2)
    private BigDecimal unitPrice;
    @Column(precision = 19, scale = 4)
    private BigDecimal unitCost;
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(precision = 19, scale = 2)
    private BigDecimal cost;
}
