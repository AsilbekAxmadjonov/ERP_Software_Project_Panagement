package org.example.erp.modules.purchasing.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "purchasing_order_counter")
@Getter @Setter
class PurchaseOrderCounterRecord {
    @Id
    private Long id;
    private long lastNumber;
}
