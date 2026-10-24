package org.example.erp.modules.purchasing.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.erp.modules.purchasing.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "purchasing_supplier_payments")
@Getter @Setter
class SupplierPaymentRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long supplierId;
    private String supplierName;
    private Long orderId;
    private String orderNumber;
    private LocalDate paymentDate;
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    private String note;
    private String createdBy;
}
