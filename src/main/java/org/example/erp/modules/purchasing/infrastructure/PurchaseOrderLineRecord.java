package org.example.erp.modules.purchasing.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "purchasing_order_lines")
@Getter @Setter
class PurchaseOrderLineRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PurchaseOrderRecord order;
    private long productId;
    private String productName;
    private String unit;
    @Column(precision = 19, scale = 3)
    private BigDecimal quantity;
    @Column(precision = 19, scale = 4)
    private BigDecimal unitCost;
}
