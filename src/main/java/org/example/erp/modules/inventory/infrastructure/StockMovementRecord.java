package org.example.erp.modules.inventory.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.erp.modules.inventory.domain.MovementType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "inventory_stock_movements")
@Getter @Setter
class StockMovementRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long productId;
    private String productName;
    private LocalDate movementDate;
    @Enumerated(EnumType.STRING)
    private MovementType type;
    @Column(precision = 19, scale = 3)
    private BigDecimal quantity;
    @Column(precision = 19, scale = 4)
    private BigDecimal unitCost;
    private String reference;
    private String reason;
}
