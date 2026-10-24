package org.example.erp.composition;

import org.example.erp.modules.accounting.application.actions.PostJournalEntry;
import org.example.erp.modules.accounting.application.commands.PostEntryCommand;
import org.example.erp.modules.accounting.application.commands.PostEntryCommand.Line;
import org.example.erp.modules.accounting.application.enums.EntryKind;
import org.example.erp.modules.accounting.application.enums.LedgerRole;
import org.example.erp.modules.purchasing.application.events.GoodsReceived;
import org.example.erp.modules.purchasing.application.events.SupplierPaid;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Turns purchasing events into ledger entries. Runs synchronously inside the publishing use case's transaction,
 * so a receipt or payment is saved together with its entry or not at all (for example in a closed period).
 */
@Component
class PurchaseLedgerPostingListener {
    private final PostJournalEntry postEntry;

    PurchaseLedgerPostingListener(PostJournalEntry postEntry) {
        this.postEntry = postEntry;
    }

    @EventListener
    void on(GoodsReceived e) {
        if (e.total().signum() == 0) return;
        postEntry.handle(new PostEntryCommand(e.receivedDate(), e.orderNumber(), "Goods received from " + e.supplierName(), List.of(
                Line.debit(LedgerRole.INVENTORY.code(), e.total(), null, e.orderNumber()),
                Line.credit(LedgerRole.PAYABLE.code(), e.total(), e.supplierId(), e.orderNumber()))), EntryKind.PURCHASE);
    }

    @EventListener
    void on(SupplierPaid e) {
        LedgerRole cashAccount = switch (e.channel()) {
            case CASH -> LedgerRole.CASH;
            case BANK -> LedgerRole.BANK;
        };
        postEntry.handle(new PostEntryCommand(e.paymentDate(), e.reference(), "Payment to " + e.supplierName(), List.of(
                Line.debit(LedgerRole.PAYABLE.code(), e.amount(), e.supplierId(), e.reference()),
                Line.credit(cashAccount.code(), e.amount(), null, e.reference()))), EntryKind.SUPPLIER_PAYMENT);
    }
}
