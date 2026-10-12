package org.example.erp.modules.sales.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sales_invoice_counter")
@Getter @Setter
class InvoiceCounterRecord {
    @Id
    private Long id;
    private long lastNumber;
}
