package org.example.erp.composition;

import org.example.erp.modules.accounting.application.actions.PostJournalEntry;
import org.example.erp.modules.accounting.application.commands.PostEntryCommand;
import org.example.erp.modules.accounting.application.commands.PostEntryCommand.Line;
import org.example.erp.modules.accounting.application.enums.EntryKind;
import org.example.erp.modules.accounting.application.enums.LedgerRole;
import org.example.erp.modules.employees.application.events.EmployeeLedgerEntryRecorded;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Posts employee account movements. Salaries Payable mirrors the employees' sub-ledger in total (credit = owed to
 * staff), so no accounting partner id is used: employee ids belong to the employees module, not to partners.
 * <ul>
 *   <li>salary accrued, bonus: Dr Salary Expense / Cr Salaries Payable</li>
 *   <li>payment, advance: Dr Salaries Payable / Cr Cash or Bank (an advance leaves the employee's balance negative)</li>
 *   <li>deduction: Dr Salaries Payable / Cr Salary Expense</li>
 * </ul>
 * Runs inside the publishing use case's transaction, so a closed period rejects the movement and its entry together.
 */
@Component
class PayrollLedgerPostingListener {
    private final PostJournalEntry postEntry;

    PayrollLedgerPostingListener(PostJournalEntry postEntry) {
        this.postEntry = postEntry;
    }

    @EventListener
    void on(EmployeeLedgerEntryRecorded e) {
        String payable = LedgerRole.SALARIES_PAYABLE.code();
        String expense = LedgerRole.SALARY_EXPENSE.code();
        String memo = e.employeeName();
        record Posting(String debit, String credit, String description) {}
        Posting posting = switch (e.type()) {
            case SALARY_ACCRUED -> new Posting(expense, payable, "Salary accrued for ");
            case BONUS -> new Posting(expense, payable, "Bonus for ");
            case PAYMENT -> new Posting(payable, cashAccount(e), "Salary payment to ");
            case ADVANCE -> new Posting(payable, cashAccount(e), "Salary advance to ");
            case DEDUCTION -> new Posting(payable, expense, "Salary deduction for ");
        };
        postEntry.handle(new PostEntryCommand(e.date(), e.reference(), posting.description() + e.employeeName(), List.of(
                Line.debit(posting.debit(), e.amount(), null, memo),
                Line.credit(posting.credit(), e.amount(), null, memo))), EntryKind.PAYROLL);
    }

    private static String cashAccount(EmployeeLedgerEntryRecorded e) {
        return switch (e.channel()) {
            case CASH -> LedgerRole.CASH.code();
            case BANK -> LedgerRole.BANK.code();
        };
    }
}
