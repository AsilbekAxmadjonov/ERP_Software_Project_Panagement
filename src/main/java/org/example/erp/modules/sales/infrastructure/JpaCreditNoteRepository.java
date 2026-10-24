package org.example.erp.modules.sales.infrastructure;

import org.example.erp.modules.sales.domain.CreditNote;
import org.example.erp.modules.sales.domain.CreditNoteLine;
import org.example.erp.modules.sales.domain.CreditNoteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class JpaCreditNoteRepository implements CreditNoteRepository {
    private final CreditNoteJpaRepository jpa;

    JpaCreditNoteRepository(CreditNoteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public CreditNote save(CreditNote c) {
        CreditNoteRecord r = new CreditNoteRecord();
        r.setNumber(c.getNumber());
        r.setInvoiceId(c.getInvoiceId());
        r.setInvoiceNumber(c.getInvoiceNumber());
        r.setCustomerId(c.getCustomerId());
        r.setCustomerName(c.getCustomerName());
        r.setCreditDate(c.getDate());
        r.setReason(c.getReason());
        r.setCreatedBy(c.getCreatedBy());
        for (CreditNoteLine l : c.getLines()) {
            CreditNoteLineRecord lr = new CreditNoteLineRecord();
            lr.setCreditNote(r);
            lr.setProductId(l.productId());
            lr.setProductName(l.productName());
            lr.setUnit(l.unit());
            lr.setQuantity(l.quantity());
            lr.setUnitPrice(l.unitPrice());
            lr.setUnitCost(l.unitCost());
            lr.setAmount(l.amount());
            lr.setCost(l.cost());
            r.getLines().add(lr);
        }
        return toDomain(jpa.save(r));
    }

    @Override
    public List<CreditNote> findAllNewestFirst() {
        return jpa.findAllByOrderByCreditDateDescIdDesc().stream().map(this::toDomain).toList();
    }

    @Override
    public List<CreditNote> findByInvoice(long invoiceId) {
        return jpa.findByInvoiceIdOrderByIdDesc(invoiceId).stream().map(this::toDomain).toList();
    }

    private CreditNote toDomain(CreditNoteRecord r) {
        List<CreditNoteLine> lines = r.getLines().stream()
                .map(l -> new CreditNoteLine(l.getProductId(), l.getProductName(), l.getUnit(), l.getQuantity(),
                        l.getUnitPrice(), l.getUnitCost(), l.getAmount(), l.getCost()))
                .toList();
        return CreditNote.restore(r.getId(), r.getNumber(), r.getInvoiceId(), r.getInvoiceNumber(), r.getCustomerId(),
                r.getCustomerName(), r.getCreditDate(), r.getReason(), lines, r.getCreatedBy());
    }
}
