package org.example.erp.modules.purchasing.application.actions;

import org.example.erp.modules.purchasing.application.dto.PurchaseOrderDTO;
import org.example.erp.modules.purchasing.domain.PurchaseOrder;
import org.example.erp.modules.purchasing.domain.PurchaseOrderRepository;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

public final class ConfirmPurchaseOrder {
    private final PurchaseOrderRepository orders;
    private final TransactionRunner tx;

    public ConfirmPurchaseOrder(PurchaseOrderRepository orders, TransactionRunner tx) {
        this.orders = orders;
        this.tx = tx;
    }

    public PurchaseOrderDTO handle(long orderId) {
        return tx.run(() -> {
            PurchaseOrder order = orders.findById(orderId).orElseThrow(() -> new NotFoundException("Purchase order not found"));
            order.confirm();
            return PurchasingMapper.toDto(orders.save(order));
        });
    }
}
