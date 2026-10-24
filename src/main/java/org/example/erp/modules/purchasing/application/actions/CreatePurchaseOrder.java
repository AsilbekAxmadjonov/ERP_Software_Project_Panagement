package org.example.erp.modules.purchasing.application.actions;

import org.example.erp.modules.purchasing.application.commands.CreatePurchaseOrderCommand;
import org.example.erp.modules.purchasing.application.contracts.PurchasableProducts;
import org.example.erp.modules.purchasing.application.contracts.PurchasableProducts.ProductInfo;
import org.example.erp.modules.purchasing.application.contracts.SupplierDirectory;
import org.example.erp.modules.purchasing.application.contracts.SupplierDirectory.SupplierInfo;
import org.example.erp.modules.purchasing.application.dto.PurchaseOrderDTO;
import org.example.erp.modules.purchasing.domain.PurchaseOrder;
import org.example.erp.modules.purchasing.domain.PurchaseOrderLine;
import org.example.erp.modules.purchasing.domain.PurchaseOrderNumberSequence;
import org.example.erp.modules.purchasing.domain.PurchaseOrderRepository;
import org.example.erp.platform.BusinessRuleException;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Creates a DRAFT order; nothing moves in stock or the ledger until it is received. */
public final class CreatePurchaseOrder {
    private final PurchaseOrderRepository orders;
    private final PurchaseOrderNumberSequence numbers;
    private final SupplierDirectory suppliers;
    private final PurchasableProducts products;
    private final TransactionRunner tx;
    private final CurrentActor actor;
    private final Clock clock;

    public CreatePurchaseOrder(PurchaseOrderRepository orders, PurchaseOrderNumberSequence numbers, SupplierDirectory suppliers,
                               PurchasableProducts products, TransactionRunner tx, CurrentActor actor, Clock clock) {
        this.orders = orders;
        this.numbers = numbers;
        this.suppliers = suppliers;
        this.products = products;
        this.tx = tx;
        this.actor = actor;
        this.clock = clock;
    }

    public PurchaseOrderDTO handle(CreatePurchaseOrderCommand c) {
        LocalDate date = c.date() == null ? LocalDate.now(clock) : c.date();
        return tx.run(() -> {
            SupplierInfo supplier = suppliers.find(c.supplierId()).filter(SupplierInfo::active)
                    .orElseThrow(() -> new NotFoundException("Supplier not found"));
            if (!supplier.supplier()) throw new BusinessRuleException(supplier.name() + " is not a supplier");

            List<PurchaseOrderLine> lines = new ArrayList<>();
            for (CreatePurchaseOrderCommand.Item item : c.items()) {
                ProductInfo product = products.find(item.productId()).filter(ProductInfo::active)
                        .orElseThrow(() -> new NotFoundException("Product not found"));
                lines.add(new PurchaseOrderLine(product.id(), product.name(), product.unit(),
                        item.quantity().setScale(3, RoundingMode.HALF_UP), item.unitCost().setScale(4, RoundingMode.HALF_UP)));
            }

            String number = String.format("PO-%05d", numbers.next());
            PurchaseOrder saved = orders.save(PurchaseOrder.create(number, supplier.id(), supplier.name(), date, lines, actor.username()));
            return PurchasingMapper.toDto(saved);
        });
    }
}
