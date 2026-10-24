package org.example.erp.modules.purchasing.infrastructure;

import org.example.erp.modules.purchasing.domain.PurchaseOrderNumberSequence;
import org.springframework.stereotype.Repository;

/** Must be called inside the creating transaction; the row lock serialises concurrent orders. */
@Repository
class JpaPurchaseOrderNumberSequence implements PurchaseOrderNumberSequence {
    private final PurchaseOrderCounterJpaRepository jpa;

    JpaPurchaseOrderNumberSequence(PurchaseOrderCounterJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public long next() {
        PurchaseOrderCounterRecord counter = jpa.findForUpdate().orElseGet(() -> {
            PurchaseOrderCounterRecord created = new PurchaseOrderCounterRecord();
            created.setId(1L);
            return jpa.saveAndFlush(created);
        });
        counter.setLastNumber(counter.getLastNumber() + 1);
        return jpa.save(counter).getLastNumber();
    }
}
