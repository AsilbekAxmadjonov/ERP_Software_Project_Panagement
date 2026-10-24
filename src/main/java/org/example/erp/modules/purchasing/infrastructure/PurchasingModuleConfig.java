package org.example.erp.modules.purchasing.infrastructure;

import org.example.erp.modules.purchasing.application.actions.*;
import org.example.erp.modules.purchasing.application.contracts.PurchasableProducts;
import org.example.erp.modules.purchasing.application.contracts.StockReceiver;
import org.example.erp.modules.purchasing.application.contracts.SupplierDirectory;
import org.example.erp.modules.purchasing.domain.PurchaseOrderNumberSequence;
import org.example.erp.modules.purchasing.domain.PurchaseOrderRepository;
import org.example.erp.modules.purchasing.domain.SupplierPaymentRepository;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.TransactionRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class PurchasingModuleConfig {

    @Bean
    CreatePurchaseOrder createPurchaseOrder(PurchaseOrderRepository orders, PurchaseOrderNumberSequence numbers,
                                            SupplierDirectory suppliers, PurchasableProducts products, TransactionRunner tx,
                                            CurrentActor actor, Clock clock) {
        return new CreatePurchaseOrder(orders, numbers, suppliers, products, tx, actor, clock);
    }

    @Bean
    ConfirmPurchaseOrder confirmPurchaseOrder(PurchaseOrderRepository orders, TransactionRunner tx) {
        return new ConfirmPurchaseOrder(orders, tx);
    }

    @Bean
    ReceivePurchaseOrder receivePurchaseOrder(PurchaseOrderRepository orders, StockReceiver stock,
                                              IntegrationEventPublisher events, TransactionRunner tx, Clock clock) {
        return new ReceivePurchaseOrder(orders, stock, events, tx, clock);
    }

    @Bean
    CancelPurchaseOrder cancelPurchaseOrder(PurchaseOrderRepository orders, TransactionRunner tx) {
        return new CancelPurchaseOrder(orders, tx);
    }

    @Bean
    FindPurchaseOrder findPurchaseOrder(PurchaseOrderRepository orders) {
        return new FindPurchaseOrder(orders);
    }

    @Bean
    ListPurchaseOrders listPurchaseOrders(PurchaseOrderRepository orders) {
        return new ListPurchaseOrders(orders);
    }

    @Bean
    RecordSupplierPayment recordSupplierPayment(PurchaseOrderRepository orders, SupplierPaymentRepository payments,
                                                SupplierDirectory suppliers, IntegrationEventPublisher events,
                                                TransactionRunner tx, CurrentActor actor, Clock clock) {
        return new RecordSupplierPayment(orders, payments, suppliers, events, tx, actor, clock);
    }

    @Bean
    ListSupplierPayments listSupplierPayments(SupplierPaymentRepository payments) {
        return new ListSupplierPayments(payments);
    }
}
