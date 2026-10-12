package org.example.erp.modules.sales.infrastructure;

import org.example.erp.modules.sales.domain.InvoiceNumberSequence;
import org.springframework.stereotype.Repository;

/** Must be called inside the issuing transaction; the row lock serialises concurrent invoices. */
@Repository
class JpaInvoiceNumberSequence implements InvoiceNumberSequence {
    private final InvoiceCounterJpaRepository jpa;

    JpaInvoiceNumberSequence(InvoiceCounterJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public long next() {
        InvoiceCounterRecord counter = jpa.findForUpdate().orElseGet(() -> {
            InvoiceCounterRecord created = new InvoiceCounterRecord();
            created.setId(1L);
            return jpa.saveAndFlush(created);
        });
        counter.setLastNumber(counter.getLastNumber() + 1);
        return jpa.save(counter).getLastNumber();
    }
}
