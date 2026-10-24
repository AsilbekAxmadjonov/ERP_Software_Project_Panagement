package org.example.erp.modules.purchasing.infrastructure;

import org.example.erp.modules.purchasing.domain.SupplierPayment;
import org.example.erp.modules.purchasing.domain.SupplierPaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class JpaSupplierPaymentRepository implements SupplierPaymentRepository {
    private final SupplierPaymentJpaRepository jpa;

    JpaSupplierPaymentRepository(SupplierPaymentJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public SupplierPayment save(SupplierPayment p) {
        SupplierPaymentRecord r = new SupplierPaymentRecord();
        r.setSupplierId(p.supplierId());
        r.setSupplierName(p.supplierName());
        r.setOrderId(p.orderId());
        r.setOrderNumber(p.orderNumber());
        r.setPaymentDate(p.date());
        r.setAmount(p.amount());
        r.setMethod(p.method());
        r.setNote(p.note());
        r.setCreatedBy(p.createdBy());
        return toDomain(jpa.save(r));
    }

    @Override
    public List<SupplierPayment> findAllNewestFirst() {
        return jpa.findAllByOrderByPaymentDateDescIdDesc().stream().map(this::toDomain).toList();
    }

    private SupplierPayment toDomain(SupplierPaymentRecord r) {
        return new SupplierPayment(r.getId(), r.getSupplierId(), r.getSupplierName(), r.getOrderId(), r.getOrderNumber(),
                r.getPaymentDate(), r.getAmount(), r.getMethod(), r.getNote(), r.getCreatedBy());
    }
}
