package org.example.erp.modules.purchasing.application.actions;

import org.example.erp.modules.purchasing.application.dto.PurchaseOrderDTO;
import org.example.erp.modules.purchasing.domain.PurchaseOrderRepository;
import org.example.erp.platform.NotFoundException;

public final class FindPurchaseOrder {
    private final PurchaseOrderRepository orders;

    public FindPurchaseOrder(PurchaseOrderRepository orders) {
        this.orders = orders;
    }

    public PurchaseOrderDTO handle(long id) {
        return orders.findById(id).map(PurchasingMapper::toDto).orElseThrow(() -> new NotFoundException("Purchase order not found"));
    }
}
