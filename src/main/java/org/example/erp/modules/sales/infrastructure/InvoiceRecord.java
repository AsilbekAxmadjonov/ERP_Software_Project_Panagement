package org.example.erp.modules.sales.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_invoices")
@Getter @Setter
class InvoiceRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String number;
    private long customerId;
    private String customerName;
    private LocalDate invoiceDate;
    @Column(precision = 19, scale = 2)
    private BigDecimal paid;
    private String createdBy;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<InvoiceLineRecord> lines = new ArrayList<>();
}
