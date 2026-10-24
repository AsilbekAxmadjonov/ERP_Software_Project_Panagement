package org.example.erp.modules.sales.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_credit_notes")
@Getter @Setter
class CreditNoteRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String number;
    private long invoiceId;
    private String invoiceNumber;
    private long customerId;
    private String customerName;
    private LocalDate creditDate;
    private String reason;
    private String createdBy;

    @OneToMany(mappedBy = "creditNote", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("id")
    private List<CreditNoteLineRecord> lines = new ArrayList<>();
}
