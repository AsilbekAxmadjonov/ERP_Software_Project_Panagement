package org.example.erp.modules.sales.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.erp.modules.sales.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sales_payments")
@Getter @Setter
class PaymentRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long customerId;
    private String customerName;
    private Long invoiceId;
    private String invoiceNumber;
    private LocalDate paymentDate;
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    private String note;
    private String createdBy;
}
