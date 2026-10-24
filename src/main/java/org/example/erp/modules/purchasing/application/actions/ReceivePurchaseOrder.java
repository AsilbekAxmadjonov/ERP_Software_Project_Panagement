package org.example.erp.modules.purchasing.application.actions;

import org.example.erp.modules.purchasing.application.contracts.StockReceiver;
import org.example.erp.modules.purchasing.application.dto.PurchaseOrderDTO;
import org.example.erp.modules.purchasing.application.events.GoodsReceived;
import org.example.erp.modules.purchasing.domain.PurchaseOrder;
import org.example.erp.modules.purchasing.domain.PurchaseOrderLine;
import org.example.erp.modules.purchasing.domain.PurchaseOrderRepository;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

import java.time.Clock;
import java.time.LocalDate;

/** Goods receipt: the full ordered quantity enters stock and the order total becomes payable to the supplier. */
public final class ReceivePurchaseOrder {
    private final PurchaseOrderRepository orders;
    private final StockReceiver stock;
    private final IntegrationEventPublisher events;
    private final TransactionRunner tx;
    private final Clock clock;

    public ReceivePurchaseOrder(PurchaseOrderRepository orders, StockReceiver stock, IntegrationEventPublisher events,
                                TransactionRunner tx, Clock clock) {
        this.orders = orders;
        this.stock = stock;
        this.events = events;
        this.tx = tx;
        this.clock = clock;
    }

    /** date is optional: today is used when absent. */
    public PurchaseOrderDTO handle(long orderId, LocalDate date) {
        LocalDate receivedOn = date == null ? LocalDate.now(clock) : date;
        return tx.run(() -> {
            PurchaseOrder order = orders.findById(orderId).orElseThrow(() -> new NotFoundException("Purchase order not found"));
            order.receive(receivedOn);
            for (PurchaseOrderLine line : order.getLines()) {
                stock.receive(line.productId(), line.quantity(), line.unitCost(), order.getNumber(), receivedOn);
            }
            PurchaseOrder saved = orders.save(order);
            events.publish(new GoodsReceived(saved.getNumber(), saved.getSupplierId(), saved.getSupplierName(), receivedOn,
                    saved.getTotal()));
            return PurchasingMapper.toDto(saved);
        });
    }
}
