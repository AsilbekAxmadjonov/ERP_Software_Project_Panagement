package org.example.erp.modules.sales.infrastructure;

import org.example.erp.modules.sales.application.actions.*;
import org.example.erp.modules.sales.application.contracts.CustomerDirectory;
import org.example.erp.modules.sales.application.contracts.ProductCatalog;
import org.example.erp.modules.sales.application.contracts.StockIssuer;
import org.example.erp.modules.sales.domain.InvoiceNumberSequence;
import org.example.erp.modules.sales.domain.InvoiceRepository;
import org.example.erp.modules.sales.domain.PaymentRepository;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.TransactionRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class SalesModuleConfig {

    @Bean
    IssueInvoice issueInvoice(InvoiceRepository invoices, InvoiceNumberSequence numbers, CustomerDirectory customers,
                              ProductCatalog catalog, StockIssuer stock, IntegrationEventPublisher events,
                              TransactionRunner tx, CurrentActor actor, Clock clock) {
        return new IssueInvoice(invoices, numbers, customers, catalog, stock, events, tx, actor, clock);
    }

    @Bean
    RecordPayment recordPayment(InvoiceRepository invoices, PaymentRepository payments, CustomerDirectory customers,
                                IntegrationEventPublisher events, TransactionRunner tx, CurrentActor actor, Clock clock) {
        return new RecordPayment(invoices, payments, customers, events, tx, actor, clock);
    }

    @Bean
    ListInvoices listInvoices(InvoiceRepository invoices) {
        return new ListInvoices(invoices);
    }

    @Bean
    ListPayments listPayments(PaymentRepository payments) {
        return new ListPayments(payments);
    }

    @Bean
    GetSalesReport getSalesReport(InvoiceRepository invoices) {
        return new GetSalesReport(invoices);
    }
}
