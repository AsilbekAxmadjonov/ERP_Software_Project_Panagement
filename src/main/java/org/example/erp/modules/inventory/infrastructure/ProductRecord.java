package org.example.erp.modules.inventory.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.erp.modules.inventory.domain.Unit;

import java.math.BigDecimal;

@Entity
@Table(name = "inventory_products")
@Getter @Setter
class ProductRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String code;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Unit unit;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal salePrice;
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal averageCost;
    private boolean active;
    @Column(precision = 19, scale = 3)
    private BigDecimal reorderLevel;
}
