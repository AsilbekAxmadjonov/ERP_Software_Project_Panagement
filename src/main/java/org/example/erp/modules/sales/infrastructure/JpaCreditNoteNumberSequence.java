package org.example.erp.modules.sales.infrastructure;

import org.example.erp.modules.sales.domain.CreditNoteNumberSequence;
import org.springframework.stereotype.Repository;

/** Must be called inside the issuing transaction; the row lock serialises concurrent credit notes. */
@Repository
class JpaCreditNoteNumberSequence implements CreditNoteNumberSequence {
    private final CreditNoteCounterJpaRepository jpa;

    JpaCreditNoteNumberSequence(CreditNoteCounterJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public long next() {
        CreditNoteCounterRecord counter = jpa.findForUpdate().orElseGet(() -> {
            CreditNoteCounterRecord created = new CreditNoteCounterRecord();
            created.setId(1L);
            return jpa.saveAndFlush(created);
        });
        counter.setLastNumber(counter.getLastNumber() + 1);
        return jpa.save(counter).getLastNumber();
    }
}
