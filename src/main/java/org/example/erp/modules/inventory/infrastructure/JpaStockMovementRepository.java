package org.example.erp.modules.inventory.infrastructure;

import org.example.erp.modules.inventory.domain.StockMovement;
import org.example.erp.modules.inventory.domain.StockMovementRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class JpaStockMovementRepository implements StockMovementRepository {
    private final StockMovementJpaRepository jpa;

    JpaStockMovementRepository(StockMovementJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public StockMovement save(StockMovement m) {
        StockMovementRecord r = new StockMovementRecord();
        r.setProductId(m.productId());
        r.setProductName(m.productName());
        r.setMovementDate(m.date());
        r.setType(m.type());
        r.setQuantity(m.quantity());
        r.setUnitCost(m.unitCost());
        r.setReference(m.reference());
        r.setReason(m.reason());
        return toDomain(jpa.save(r));
    }

    @Override
    public List<StockMovement> findRecent() {
        return jpa.findTop300ByOrderByIdDesc().stream().map(this::toDomain).toList();
    }

    @Override
    public List<StockMovement> findByProduct(long productId) {
        return jpa.findByProductIdOrderByIdDesc(productId).stream().map(this::toDomain).toList();
    }

    private StockMovement toDomain(StockMovementRecord r) {
        return new StockMovement(r.getId(), r.getProductId(), r.getProductName(), r.getMovementDate(), r.getType(),
                r.getQuantity(), r.getUnitCost(), r.getReference(), r.getReason());
    }
}
