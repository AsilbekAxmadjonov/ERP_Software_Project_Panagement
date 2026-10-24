package org.example.erp.modules.purchasing.infrastructure;

import org.example.erp.modules.purchasing.domain.PurchaseOrder;
import org.example.erp.modules.purchasing.domain.PurchaseOrderLine;
import org.example.erp.modules.purchasing.domain.PurchaseOrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaPurchaseOrderRepository implements PurchaseOrderRepository {
    private final PurchaseOrderJpaRepository jpa;

    JpaPurchaseOrderRepository(PurchaseOrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<PurchaseOrder> findById(long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<PurchaseOrder> findAllNewestFirst() {
        return jpa.findAllByOrderByOrderDateDescIdDesc().stream().map(this::toDomain).toList();
    }

    @Override
    public PurchaseOrder save(PurchaseOrder o) {
        // order lines are fixed at creation, so an existing record only has its status, receipt and payments updated
        PurchaseOrderRecord r = o.getId() == null ? new PurchaseOrderRecord() : jpa.getReferenceById(o.getId());
        r.setNumber(o.getNumber());
        r.setSupplierId(o.getSupplierId());
        r.setSupplierName(o.getSupplierName());
        r.setOrderDate(o.getOrderDate());
        r.setStatus(o.getStatus());
        r.setReceivedDate(o.getReceivedDate());
        r.setPaid(o.getPaid());
        r.setCreatedBy(o.getCreatedBy());
        if (o.getId() == null) {
            for (PurchaseOrderLine l : o.getLines()) {
                PurchaseOrderLineRecord lr = new PurchaseOrderLineRecord();
                lr.setOrder(r);
                lr.setProductId(l.productId());
                lr.setProductName(l.productName());
                lr.setUnit(l.unit());
                lr.setQuantity(l.quantity());
                lr.setUnitCost(l.unitCost());
                r.getLines().add(lr);
            }
        }
        return toDomain(jpa.save(r));
    }

    private PurchaseOrder toDomain(PurchaseOrderRecord r) {
        List<PurchaseOrderLine> lines = r.getLines().stream()
                .map(l -> new PurchaseOrderLine(l.getProductId(), l.getProductName(), l.getUnit(), l.getQuantity(), l.getUnitCost()))
                .toList();
        return PurchaseOrder.restore(r.getId(), r.getNumber(), r.getSupplierId(), r.getSupplierName(), r.getOrderDate(), lines,
                r.getStatus(), r.getReceivedDate(), r.getPaid(), r.getCreatedBy());
    }
}
