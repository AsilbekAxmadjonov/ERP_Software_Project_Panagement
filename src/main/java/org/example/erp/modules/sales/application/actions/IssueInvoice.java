package org.example.erp.modules.sales.application.actions;

import org.example.erp.modules.sales.application.commands.IssueInvoiceCommand;
import org.example.erp.modules.sales.application.contracts.CustomerDirectory;
import org.example.erp.modules.sales.application.contracts.CustomerDirectory.CustomerInfo;
import org.example.erp.modules.sales.application.contracts.ProductCatalog;
import org.example.erp.modules.sales.application.contracts.ProductCatalog.ProductInfo;
import org.example.erp.modules.sales.application.contracts.StockIssuer;
import org.example.erp.modules.sales.application.dto.InvoiceDTO;
import org.example.erp.modules.sales.application.events.InvoiceIssued;
import org.example.erp.modules.sales.domain.Invoice;
import org.example.erp.modules.sales.domain.InvoiceLine;
import org.example.erp.modules.sales.domain.InvoiceNumberSequence;
import org.example.erp.modules.sales.domain.InvoiceRepository;
import org.example.erp.platform.BusinessRuleException;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class IssueInvoice {
    private final InvoiceRepository invoices;
    private final InvoiceNumberSequence numbers;
    private final CustomerDirectory customers;
    private final ProductCatalog catalog;
    private final StockIssuer stock;
    private final IntegrationEventPublisher events;
    private final TransactionRunner tx;
    private final CurrentActor actor;
    private final Clock clock;

    public IssueInvoice(InvoiceRepository invoices, InvoiceNumberSequence numbers, CustomerDirectory customers,
                        ProductCatalog catalog, StockIssuer stock, IntegrationEventPublisher events, TransactionRunner tx,
                        CurrentActor actor, Clock clock) {
        this.invoices = invoices;
        this.numbers = numbers;
        this.customers = customers;
        this.catalog = catalog;
        this.stock = stock;
        this.events = events;
        this.tx = tx;
        this.actor = actor;
        this.clock = clock;
    }

    public InvoiceDTO handle(IssueInvoiceCommand c) {
        LocalDate date = c.date() == null ? LocalDate.now(clock) : c.date();
        return tx.run(() -> {
            CustomerInfo customer = customers.find(c.customerId()).filter(CustomerInfo::active)
                    .orElseThrow(() -> new NotFoundException("Customer not found"));
            if (!customer.customer()) throw new BusinessRuleException(customer.name() + " is not a customer");

            String number = String.format("INV-%05d", numbers.next());
            List<InvoiceLine> lines = new ArrayList<>();
            for (IssueInvoiceCommand.Item item : c.items()) {
                ProductInfo product = catalog.find(item.productId()).filter(ProductInfo::active)
                        .orElseThrow(() -> new NotFoundException("Product not found"));
                var quantity = item.quantity().setScale(3, RoundingMode.HALF_UP);
                var price = (item.unitPrice() == null ? product.salePrice() : item.unitPrice()).setScale(2, RoundingMode.HALF_UP);
                var unitCost = stock.issue(product.id(), quantity, number, date);
                lines.add(new InvoiceLine(product.id(), product.name(), product.unit(), quantity, price, unitCost));
            }

            Invoice saved = invoices.save(Invoice.issue(number, customer.id(), customer.name(), date, lines, actor.username()));
            events.publish(new InvoiceIssued(saved.getNumber(), saved.getCustomerId(), saved.getCustomerName(),
                    saved.getInvoiceDate(), saved.getTotal(), saved.getCost()));
            return SalesMapper.toDto(saved);
        });
    }
}
