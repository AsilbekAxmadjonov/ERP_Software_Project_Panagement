package org.example.erp.modules.sales.application.actions;

import org.example.erp.modules.sales.application.commands.IssueCreditNoteCommand;
import org.example.erp.modules.sales.application.contracts.StockReturner;
import org.example.erp.modules.sales.application.dto.CreditNoteDTO;
import org.example.erp.modules.sales.application.events.CreditNoteIssued;
import org.example.erp.modules.sales.domain.CreditNote;
import org.example.erp.modules.sales.domain.CreditNoteLine;
import org.example.erp.modules.sales.domain.CreditNoteNumberSequence;
import org.example.erp.modules.sales.domain.CreditNoteRepository;
import org.example.erp.modules.sales.domain.Invoice;
import org.example.erp.modules.sales.domain.InvoiceRepository;
import org.example.erp.modules.sales.domain.ReturnItem;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

/** Sales return: takes goods back into stock at their original cost and credits the customer at the sold price. */
public final class IssueCreditNote {
    private final InvoiceRepository invoices;
    private final CreditNoteRepository creditNotes;
    private final CreditNoteNumberSequence numbers;
    private final StockReturner stock;
    private final IntegrationEventPublisher events;
    private final TransactionRunner tx;
    private final CurrentActor actor;
    private final Clock clock;

    public IssueCreditNote(InvoiceRepository invoices, CreditNoteRepository creditNotes, CreditNoteNumberSequence numbers,
                           StockReturner stock, IntegrationEventPublisher events, TransactionRunner tx, CurrentActor actor,
                           Clock clock) {
        this.invoices = invoices;
        this.creditNotes = creditNotes;
        this.numbers = numbers;
        this.stock = stock;
        this.events = events;
        this.tx = tx;
        this.actor = actor;
        this.clock = clock;
    }

    public CreditNoteDTO handle(IssueCreditNoteCommand c) {
        LocalDate date = c.date() == null ? LocalDate.now(clock) : c.date();
        String reason = c.reason() == null || c.reason().isBlank() ? null : c.reason().trim();
        var items = c.items().stream()
                .map(i -> new ReturnItem(i.productId(), i.quantity().setScale(3, RoundingMode.HALF_UP))).toList();
        return tx.run(() -> {
            Invoice invoice = invoices.findById(c.invoiceId()).orElseThrow(() -> new NotFoundException("Invoice not found"));
            String number = String.format("CN-%05d", numbers.next());
            CreditNote note = invoice.creditReturn(number, date, reason, items, actor.username());
            invoices.save(invoice);
            CreditNote saved = creditNotes.save(note);
            for (CreditNoteLine line : saved.getLines()) {
                stock.returnToStock(line.productId(), line.quantity(), line.unitCost(), saved.getNumber(), date);
            }
            events.publish(new CreditNoteIssued(saved.getNumber(), saved.getInvoiceNumber(), saved.getCustomerId(),
                    saved.getCustomerName(), date, saved.getTotal(), saved.getCost()));
            return SalesMapper.toDto(saved);
        });
    }
}
