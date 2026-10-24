package org.example.erp.modules.purchasing.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.erp.modules.purchasing.domain.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchasing_orders")
@Getter @Setter
class PurchaseOrderRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String number;
    private long supplierId;
    private String supplierName;
    private LocalDate orderDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private PurchaseOrderStatus status;
    private LocalDate receivedDate;
    @Column(precision = 19, scale = 2)
    private BigDecimal paid;
    private String createdBy;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("id")
    private List<PurchaseOrderLineRecord> lines = new ArrayList<>();
}
