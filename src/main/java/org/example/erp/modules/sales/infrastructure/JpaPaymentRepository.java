package org.example.erp.modules.sales.infrastructure;

import org.example.erp.modules.sales.domain.Payment;
import org.example.erp.modules.sales.domain.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class JpaPaymentRepository implements PaymentRepository {
    private final PaymentJpaRepository jpa;

    JpaPaymentRepository(PaymentJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Payment save(Payment p) {
        PaymentRecord r = new PaymentRecord();
        r.setCustomerId(p.customerId());
        r.setCustomerName(p.customerName());
        r.setInvoiceId(p.invoiceId());
        r.setInvoiceNumber(p.invoiceNumber());
        r.setPaymentDate(p.date());
        r.setAmount(p.amount());
        r.setMethod(p.method());
        r.setNote(p.note());
        r.setCreatedBy(p.createdBy());
        return toDomain(jpa.save(r));
    }

    @Override
    public List<Payment> findAllNewestFirst() {
        return jpa.findAllByOrderByPaymentDateDescIdDesc().stream().map(this::toDomain).toList();
    }

    private Payment toDomain(PaymentRecord r) {
        return new Payment(r.getId(), r.getCustomerId(), r.getCustomerName(), r.getInvoiceId(), r.getInvoiceNumber(),
                r.getPaymentDate(), r.getAmount(), r.getMethod(), r.getNote(), r.getCreatedBy());
    }
}
