package org.example.erp.composition;

import org.example.erp.modules.accounting.application.actions.PostJournalEntry;
import org.example.erp.modules.accounting.application.commands.PostEntryCommand;
import org.example.erp.modules.accounting.application.commands.PostEntryCommand.Line;
import org.example.erp.modules.accounting.application.enums.EntryKind;
import org.example.erp.modules.accounting.application.enums.LedgerRole;
import org.example.erp.modules.inventory.application.events.StockAdjusted;
import org.example.erp.modules.sales.application.events.InvoiceIssued;
import org.example.erp.modules.sales.application.events.PaymentReceived;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Turns sales and inventory events into balanced ledger entries. Listeners run synchronously inside the
 * publishing use case's transaction, so a business action and its ledger entry are saved together or not at all.
 */
@Component
class LedgerPostingListener {
    private final PostJournalEntry postEntry;
    private final Clock clock;

    LedgerPostingListener(PostJournalEntry postEntry, Clock clock) {
        this.postEntry = postEntry;
        this.clock = clock;
    }

    @EventListener
    void on(InvoiceIssued e) {
        List<Line> lines = new ArrayList<>();
        lines.add(Line.debit(LedgerRole.RECEIVABLE.code(), e.total(), e.customerId(), e.invoiceNumber()));
        lines.add(Line.credit(LedgerRole.SALES_REVENUE.code(), e.total(), null, e.invoiceNumber()));
        if (e.cost().signum() > 0) {
            lines.add(Line.debit(LedgerRole.COST_OF_GOODS_SOLD.code(), e.cost(), null, e.invoiceNumber()));
            lines.add(Line.credit(LedgerRole.INVENTORY.code(), e.cost(), null, e.invoiceNumber()));
        }
        postEntry.handle(new PostEntryCommand(e.invoiceDate(), e.invoiceNumber(), "Sale to " + e.customerName(), lines), EntryKind.SALE);
    }

    @EventListener
    void on(PaymentReceived e) {
        LedgerRole cashAccount = switch (e.channel()) {
            case CASH -> LedgerRole.CASH;
            case BANK -> LedgerRole.BANK;
        };
        postEntry.handle(new PostEntryCommand(e.paymentDate(), e.reference(), "Payment from " + e.customerName(), List.of(
                Line.debit(cashAccount.code(), e.amount(), null, e.reference()),
                Line.credit(LedgerRole.RECEIVABLE.code(), e.amount(), e.customerId(), e.reference()))), EntryKind.PAYMENT);
    }

    @EventListener
    void on(StockAdjusted e) {
        LocalDate today = LocalDate.now(clock);
        String reference = "ADJ-" + e.productId();
        List<Line> lines = switch (e.change()) {
            case ADDED -> List.of(
                    Line.debit(LedgerRole.INVENTORY.code(), e.value(), null, e.productName()),
                    Line.credit(LedgerRole.EQUITY.code(), e.value(), null, e.productName()));
            case WRITTEN_OFF -> List.of(
                    Line.debit(LedgerRole.INVENTORY_LOSS.code(), e.value(), null, e.productName()),
                    Line.credit(LedgerRole.INVENTORY.code(), e.value(), null, e.productName()));
        };
        postEntry.handle(new PostEntryCommand(today, reference, "Stock adjustment: " + e.reason(), lines), EntryKind.STOCK_ADJUSTMENT);
    }
}
