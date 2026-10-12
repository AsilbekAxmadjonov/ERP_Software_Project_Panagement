package org.example.erp.modules.sales.application.actions;

import org.example.erp.modules.sales.application.commands.RecordPaymentCommand;
import org.example.erp.modules.sales.application.contracts.CustomerDirectory;
import org.example.erp.modules.sales.application.dto.PaymentDTO;
import org.example.erp.modules.sales.application.events.PaymentReceived;
import org.example.erp.modules.sales.domain.Invoice;
import org.example.erp.modules.sales.domain.InvoiceRepository;
import org.example.erp.modules.sales.domain.Payment;
import org.example.erp.modules.sales.domain.PaymentMethod;
import org.example.erp.modules.sales.domain.PaymentRepository;
import org.example.erp.platform.BusinessRuleException;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

public final class RecordPayment {
    private final InvoiceRepository invoices;
    private final PaymentRepository payments;
    private final CustomerDirectory customers;
    private final IntegrationEventPublisher events;
    private final TransactionRunner tx;
    private final CurrentActor actor;
    private final Clock clock;

    public RecordPayment(InvoiceRepository invoices, PaymentRepository payments, CustomerDirectory customers,
                         IntegrationEventPublisher events, TransactionRunner tx, CurrentActor actor, Clock clock) {
        this.invoices = invoices;
        this.payments = payments;
        this.customers = customers;
        this.events = events;
        this.tx = tx;
        this.actor = actor;
        this.clock = clock;
    }

    public PaymentDTO handle(RecordPaymentCommand c) {
        LocalDate date = c.date() == null ? LocalDate.now(clock) : c.date();
        var amount = c.amount().setScale(2, RoundingMode.HALF_UP);
        return tx.run(() -> {
            var customer = customers.find(c.customerId()).orElseThrow(() -> new NotFoundException("Customer not found"));

            Long invoiceId = null;
            String invoiceNumber = null;
            if (c.invoiceId() != null) {
                Invoice invoice = invoices.findById(c.invoiceId()).orElseThrow(() -> new NotFoundException("Invoice not found"));
                if (invoice.getCustomerId() != customer.id()) throw new BusinessRuleException("Invoice belongs to another customer");
                invoice.applyPayment(amount);
                invoices.save(invoice);
                invoiceId = invoice.getId();
                invoiceNumber = invoice.getNumber();
            }

            Payment saved = payments.save(new Payment(null, customer.id(), customer.name(), invoiceId, invoiceNumber, date,
                    amount, PaymentMethod.valueOf(c.method().name()), c.note(), actor.username()));
            String reference = invoiceNumber == null ? "PAY-" + saved.id() : "PAY-" + invoiceNumber;
            events.publish(new PaymentReceived(reference, customer.id(), customer.name(), date, amount, c.method(), invoiceNumber));
            return SalesMapper.toDto(saved);
        });
    }
}
