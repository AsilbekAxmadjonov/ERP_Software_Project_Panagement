package org.example.erp.modules.purchasing.application.actions;

import org.example.erp.modules.purchasing.application.dto.PurchaseOrderDTO;
import org.example.erp.modules.purchasing.application.dto.PurchaseOrderLineDTO;
import org.example.erp.modules.purchasing.application.dto.SupplierPaymentDTO;
import org.example.erp.modules.purchasing.application.enums.OrderStatus;
import org.example.erp.modules.purchasing.application.enums.PaymentChannel;
import org.example.erp.modules.purchasing.application.enums.PaymentProgress;
import org.example.erp.modules.purchasing.domain.PurchaseOrder;
import org.example.erp.modules.purchasing.domain.SupplierPayment;

final class PurchasingMapper {
    private PurchasingMapper() {}

    static PurchaseOrderDTO toDto(PurchaseOrder o) {
        PaymentProgress progress = o.getPaid().signum() == 0 || o.billed().signum() == 0 ? PaymentProgress.UNPAID
                : o.outstanding().signum() == 0 ? PaymentProgress.PAID : PaymentProgress.PARTIAL;
        return new PurchaseOrderDTO(o.getId(), o.getNumber(), o.getSupplierId(), o.getSupplierName(), o.getOrderDate(),
                OrderStatus.valueOf(o.getStatus().name()), o.getReceivedDate(), o.getTotal(), o.billed(), o.getPaid(),
                o.outstanding(), progress, o.getCreatedBy(),
                o.getLines().stream().map(l -> new PurchaseOrderLineDTO(l.productId(), l.productName(), l.unit(),
                        l.quantity(), l.unitCost(), l.lineTotal())).toList());
    }

    static SupplierPaymentDTO toDto(SupplierPayment p) {
        return new SupplierPaymentDTO(p.id(), p.supplierId(), p.supplierName(), p.orderId(), p.orderNumber(), p.date(),
                p.amount(), PaymentChannel.valueOf(p.method().name()), p.note(), p.createdBy());
    }
}
