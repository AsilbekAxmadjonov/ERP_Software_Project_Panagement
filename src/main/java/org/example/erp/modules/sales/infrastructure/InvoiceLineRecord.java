package org.example.erp.modules.sales.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_invoice_lines")
@Getter @Setter
class InvoiceLineRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private InvoiceRecord invoice;
    private long productId;
    private String productName;
    private String unit;
    @Column(precision = 19, scale = 3)
    private BigDecimal quantity;
    @Column(precision = 19, scale = 2)
    private BigDecimal unitPrice;
    @Column(precision = 19, scale = 4)
    private BigDecimal unitCost;
}
