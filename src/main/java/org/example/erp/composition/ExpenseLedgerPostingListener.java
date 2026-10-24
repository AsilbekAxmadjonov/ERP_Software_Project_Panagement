package org.example.erp.composition;

import org.example.erp.modules.accounting.application.actions.PostJournalEntry;
import org.example.erp.modules.accounting.application.commands.PostEntryCommand;
import org.example.erp.modules.accounting.application.commands.PostEntryCommand.Line;
import org.example.erp.modules.accounting.application.enums.EntryKind;
import org.example.erp.modules.accounting.application.enums.LedgerRole;
import org.example.erp.modules.expenses.application.enums.EntryType;
import org.example.erp.modules.expenses.application.enums.PaymentChannel;
import org.example.erp.modules.expenses.application.events.CashEntryCancelled;
import org.example.erp.modules.expenses.application.events.CashEntryRecorded;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Posts expenses (Dr category expense account / Cr cash or bank) and other income (Dr cash or bank / Cr category
 * revenue account). A cancellation posts the same lines swapped. Runs inside the publishing use case's transaction,
 * so a closed accounting period rejects the record and its entry together.
 */
@Component
class ExpenseLedgerPostingListener {
    private final PostJournalEntry postEntry;

    ExpenseLedgerPostingListener(PostJournalEntry postEntry) {
        this.postEntry = postEntry;
    }

    @EventListener
    void on(CashEntryRecorded e) {
        String memo = e.description() == null ? e.categoryName() : e.description();
        String description = switch (e.type()) {
            case EXPENSE -> "Expense: " + e.categoryName();
            case INCOME -> "Other income: " + e.categoryName();
        };
        postEntry.handle(new PostEntryCommand(e.date(), e.reference(), description,
                lines(e.type(), e.accountCode(), cashAccount(e.channel()), e.amount(), memo, false)), entryKind(e.type()));
    }

    @EventListener
    void on(CashEntryCancelled e) {
        postEntry.handle(new PostEntryCommand(e.cancelledOn(), e.reference(), "Cancellation of " + e.reference() + " (" + e.categoryName() + ")",
                lines(e.type(), e.accountCode(), cashAccount(e.channel()), e.amount(), "Cancelled", true)), entryKind(e.type()));
    }

    private static List<Line> lines(EntryType type, String categoryAccount, String cashAccount, BigDecimal amount, String memo,
                                    boolean reverse) {
        String debit = switch (type) {
            case EXPENSE -> categoryAccount;
            case INCOME -> cashAccount;
        };
        String credit = switch (type) {
            case EXPENSE -> cashAccount;
            case INCOME -> categoryAccount;
        };
        return reverse
                ? List.of(Line.debit(credit, amount, null, memo), Line.credit(debit, amount, null, memo))
                : List.of(Line.debit(debit, amount, null, memo), Line.credit(credit, amount, null, memo));
    }

    private static String cashAccount(PaymentChannel channel) {
        return switch (channel) {
            case CASH -> LedgerRole.CASH.code();
            case BANK -> LedgerRole.BANK.code();
        };
    }

    private static EntryKind entryKind(EntryType type) {
        return switch (type) {
            case EXPENSE -> EntryKind.EXPENSE;
            case INCOME -> EntryKind.OTHER_INCOME;
        };
    }
}
