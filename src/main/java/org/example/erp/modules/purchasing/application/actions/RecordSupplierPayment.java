package org.example.erp.modules.purchasing.application.actions;

import org.example.erp.modules.purchasing.application.commands.RecordSupplierPaymentCommand;
import org.example.erp.modules.purchasing.application.contracts.SupplierDirectory;
import org.example.erp.modules.purchasing.application.dto.SupplierPaymentDTO;
import org.example.erp.modules.purchasing.application.events.SupplierPaid;
import org.example.erp.modules.purchasing.domain.PaymentMethod;
import org.example.erp.modules.purchasing.domain.PurchaseOrder;
import org.example.erp.modules.purchasing.domain.PurchaseOrderRepository;
import org.example.erp.modules.purchasing.domain.SupplierPayment;
import org.example.erp.modules.purchasing.domain.SupplierPaymentRepository;
import org.example.erp.platform.BusinessRuleException;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

public final class RecordSupplierPayment {
    private final PurchaseOrderRepository orders;
    private final SupplierPaymentRepository payments;
    private final SupplierDirectory suppliers;
    private final IntegrationEventPublisher events;
    private final TransactionRunner tx;
    private final CurrentActor actor;
    private final Clock clock;

    public RecordSupplierPayment(PurchaseOrderRepository orders, SupplierPaymentRepository payments, SupplierDirectory suppliers,
                                 IntegrationEventPublisher events, TransactionRunner tx, CurrentActor actor, Clock clock) {
        this.orders = orders;
        this.payments = payments;
        this.suppliers = suppliers;
        this.events = events;
        this.tx = tx;
        this.actor = actor;
        this.clock = clock;
    }

    public SupplierPaymentDTO handle(RecordSupplierPaymentCommand c) {
        LocalDate date = c.date() == null ? LocalDate.now(clock) : c.date();
        var amount = c.amount().setScale(2, RoundingMode.HALF_UP);
        return tx.run(() -> {
            var supplier = suppliers.find(c.supplierId()).orElseThrow(() -> new NotFoundException("Supplier not found"));
            if (!supplier.supplier()) throw new BusinessRuleException(supplier.name() + " is not a supplier");

            Long orderId = null;
            String orderNumber = null;
            if (c.orderId() != null) {
                PurchaseOrder order = orders.findById(c.orderId()).orElseThrow(() -> new NotFoundException("Purchase order not found"));
                if (order.getSupplierId() != supplier.id()) throw new BusinessRuleException("Order belongs to another supplier");
                order.applyPayment(amount);
                orders.save(order);
                orderId = order.getId();
                orderNumber = order.getNumber();
            }

            SupplierPayment saved = payments.save(new SupplierPayment(null, supplier.id(), supplier.name(), orderId, orderNumber,
                    date, amount, PaymentMethod.valueOf(c.method().name()), c.note(), actor.username()));
            String reference = orderNumber == null ? "SPAY-" + saved.id() : "SPAY-" + orderNumber;
            events.publish(new SupplierPaid(reference, supplier.id(), supplier.name(), date, amount, c.method(), orderNumber));
            return PurchasingMapper.toDto(saved);
        });
    }
}
