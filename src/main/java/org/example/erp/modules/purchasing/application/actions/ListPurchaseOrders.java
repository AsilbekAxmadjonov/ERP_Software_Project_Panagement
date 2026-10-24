package org.example.erp.modules.purchasing.application.actions;

import org.example.erp.modules.purchasing.application.dto.PurchaseOrderDTO;
import org.example.erp.modules.purchasing.domain.PurchaseOrderRepository;

import java.util.List;

public final class ListPurchaseOrders {
    private final PurchaseOrderRepository orders;

    public ListPurchaseOrders(PurchaseOrderRepository orders) {
        this.orders = orders;
    }

    public List<PurchaseOrderDTO> handle() {
        return orders.findAllNewestFirst().stream().map(PurchasingMapper::toDto).toList();
    }
}
