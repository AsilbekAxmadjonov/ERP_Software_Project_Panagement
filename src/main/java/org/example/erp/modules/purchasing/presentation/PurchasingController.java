package org.example.erp.modules.purchasing.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.example.erp.modules.purchasing.application.actions.*;
import org.example.erp.modules.purchasing.application.commands.CreatePurchaseOrderCommand;
import org.example.erp.modules.purchasing.application.commands.RecordSupplierPaymentCommand;
import org.example.erp.modules.purchasing.application.dto.PurchaseOrderDTO;
import org.example.erp.modules.purchasing.application.dto.SupplierPaymentDTO;
import org.example.erp.modules.purchasing.application.enums.PaymentChannel;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/purchase")
class PurchasingController {
    private final CreatePurchaseOrder createOrder;
    private final ConfirmPurchaseOrder confirmOrder;
    private final ReceivePurchaseOrder receiveOrder;
    private final CancelPurchaseOrder cancelOrder;
    private final FindPurchaseOrder findOrder;
    private final ListPurchaseOrders listOrders;
    private final RecordSupplierPayment recordPayment;
    private final ListSupplierPayments listPayments;

    PurchasingController(CreatePurchaseOrder createOrder, ConfirmPurchaseOrder confirmOrder, ReceivePurchaseOrder receiveOrder,
                         CancelPurchaseOrder cancelOrder, FindPurchaseOrder findOrder, ListPurchaseOrders listOrders,
                         RecordSupplierPayment recordPayment, ListSupplierPayments listPayments) {
        this.createOrder = createOrder;
        this.confirmOrder = confirmOrder;
        this.receiveOrder = receiveOrder;
        this.cancelOrder = cancelOrder;
        this.findOrder = findOrder;
        this.listOrders = listOrders;
        this.recordPayment = recordPayment;
        this.listPayments = listPayments;
    }

    record ItemRequest(@NotNull Long productId, @NotNull BigDecimal quantity, @NotNull BigDecimal unitCost) {}

    record OrderRequest(@NotNull Long supplierId, LocalDate date, @NotEmpty @Valid List<ItemRequest> items) {}

    record ReceiveRequest(LocalDate date) {}

    record PaymentRequest(@NotNull Long supplierId, Long orderId, @NotNull BigDecimal amount,
                          @NotNull PaymentChannel method, LocalDate date, String note) {}

    @GetMapping("/orders")
    List<PurchaseOrderDTO> orders() {
        return listOrders.handle();
    }

    @PostMapping("/orders")
    PurchaseOrderDTO create(@RequestBody @Valid OrderRequest r) {
        return createOrder.handle(new CreatePurchaseOrderCommand(r.supplierId(), r.date(),
                r.items().stream().map(i -> new CreatePurchaseOrderCommand.Item(i.productId(), i.quantity(), i.unitCost())).toList()));
    }

    @GetMapping("/orders/{id}")
    PurchaseOrderDTO order(@PathVariable long id) {
        return findOrder.handle(id);
    }

    @PostMapping("/orders/{id}/confirm")
    PurchaseOrderDTO confirm(@PathVariable long id) {
        return confirmOrder.handle(id);
    }

    /** Body is optional: without a date the goods are received today. */
    @PostMapping("/orders/{id}/receive")
    PurchaseOrderDTO receive(@PathVariable long id, @RequestBody(required = false) ReceiveRequest r) {
        return receiveOrder.handle(id, r == null ? null : r.date());
    }

    @PostMapping("/orders/{id}/cancel")
    PurchaseOrderDTO cancel(@PathVariable long id) {
        return cancelOrder.handle(id);
    }

    @GetMapping("/payments")
    List<SupplierPaymentDTO> payments() {
        return listPayments.handle();
    }

    @PostMapping("/payments")
    SupplierPaymentDTO pay(@RequestBody @Valid PaymentRequest r) {
        return recordPayment.handle(new RecordSupplierPaymentCommand(r.supplierId(), r.orderId(), r.amount(), r.method(),
                r.date(), r.note()));
    }
}
