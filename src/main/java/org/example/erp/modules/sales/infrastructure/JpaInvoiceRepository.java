package org.example.erp.modules.sales.infrastructure;

import org.example.erp.modules.sales.domain.Invoice;
import org.example.erp.modules.sales.domain.InvoiceLine;
import org.example.erp.modules.sales.domain.InvoiceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaInvoiceRepository implements InvoiceRepository {
    private final InvoiceJpaRepository jpa;

    JpaInvoiceRepository(InvoiceJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Invoice> findById(long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Invoice> findAllNewestFirst() {
        return jpa.findAllByOrderByInvoiceDateDescIdDesc().stream().map(this::toDomain).toList();
    }

    @Override
    public Invoice save(Invoice i) {
        // an issued invoice's lines never change, so an existing record only has its paid amount updated
        InvoiceRecord r = i.getId() == null ? new InvoiceRecord() : jpa.getReferenceById(i.getId());
        r.setNumber(i.getNumber());
        r.setCustomerId(i.getCustomerId());
        r.setCustomerName(i.getCustomerName());
        r.setInvoiceDate(i.getInvoiceDate());
        r.setPaid(i.getPaid());
        r.setCreatedBy(i.getCreatedBy());
        if (i.getId() == null) {
            for (InvoiceLine l : i.getLines()) {
                InvoiceLineRecord lr = new InvoiceLineRecord();
                lr.setInvoice(r);
                lr.setProductId(l.productId());
                lr.setProductName(l.productName());
                lr.setUnit(l.unit());
                lr.setQuantity(l.quantity());
                lr.setUnitPrice(l.unitPrice());
                lr.setUnitCost(l.unitCost());
                r.getLines().add(lr);
            }
        }
        return toDomain(jpa.save(r));
    }

    private Invoice toDomain(InvoiceRecord r) {
        List<InvoiceLine> lines = r.getLines().stream()
                .map(l -> new InvoiceLine(l.getProductId(), l.getProductName(), l.getUnit(), l.getQuantity(), l.getUnitPrice(), l.getUnitCost()))
                .toList();
        return Invoice.restore(r.getId(), r.getNumber(), r.getCustomerId(), r.getCustomerName(), r.getInvoiceDate(), lines,
                r.getPaid(), r.getCreatedBy());
    }
}
