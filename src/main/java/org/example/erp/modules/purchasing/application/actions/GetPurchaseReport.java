package org.example.erp.modules.purchasing.application.actions;

import org.example.erp.modules.purchasing.application.dto.PurchaseReportRowDTO;
import org.example.erp.modules.purchasing.domain.PurchaseOrder;
import org.example.erp.modules.purchasing.domain.PurchaseOrderLine;
import org.example.erp.modules.purchasing.domain.PurchaseOrderRepository;
import org.example.erp.modules.purchasing.domain.PurchaseOrderStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * One row per item of a received order, filtered by receipt date range, supplier and product (all filters optional).
 * Drafts, confirmed and cancelled orders are left out: only received goods are actual purchases.
 */
public final class GetPurchaseReport {
    private final PurchaseOrderRepository orders;

    public GetPurchaseReport(PurchaseOrderRepository orders) {
        this.orders = orders;
    }

    public List<PurchaseReportRowDTO> handle(LocalDate from, LocalDate to, Long supplierId, Long productId) {
        List<PurchaseReportRowDTO> rows = new ArrayList<>();
        for (PurchaseOrder order : orders.findAllNewestFirst()) {
            if (order.getStatus() != PurchaseOrderStatus.RECEIVED) continue;
            LocalDate received = order.getReceivedDate();
            if (from != null && received.isBefore(from)) continue;
            if (to != null && received.isAfter(to)) continue;
            if (supplierId != null && order.getSupplierId() != supplierId) continue;
            for (PurchaseOrderLine line : order.getLines()) {
                if (productId != null && line.productId() != productId) continue;
                rows.add(new PurchaseReportRowDTO(received, order.getId(), order.getNumber(), order.getSupplierId(),
                        order.getSupplierName(), line.productId(), line.productName(), line.unit(), line.quantity(),
                        line.unitCost(), line.lineTotal()));
            }
        }
        return rows;
    }
}
