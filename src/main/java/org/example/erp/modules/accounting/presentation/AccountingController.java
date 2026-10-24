package org.example.erp.modules.accounting.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.example.erp.modules.accounting.application.actions.*;
import org.example.erp.modules.accounting.application.commands.CreateAccountCommand;
import org.example.erp.modules.accounting.application.commands.PostEntryCommand;
import org.example.erp.modules.accounting.application.dto.*;
import org.example.erp.modules.accounting.application.enums.AccountKind;
import org.example.erp.modules.accounting.application.enums.EntryKind;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/accounting")
class AccountingController {
    private final ListAccounts listAccounts;
    private final CreateAccount createAccount;
    private final UpdateAccount updateAccount;
    private final PostJournalEntry postEntry;
    private final ReverseJournalEntry reverseEntry;
    private final ListJournalEntries listEntries;
    private final GetTrialBalance trialBalance;
    private final GetAccountLedger accountLedger;
    private final GetPartnerLedger partnerLedger;
    private final GetPeriodLock periodLock;
    private final ClosePeriod closePeriod;
    private final ReopenPeriod reopenPeriod;
    private final GetProfitAndLoss profitAndLoss;
    private final GetBalanceSheet balanceSheet;
    private final GetCashFlow cashFlow;
    private final GetAccountingSummary summary;

    AccountingController(ListAccounts listAccounts, CreateAccount createAccount, UpdateAccount updateAccount,
                         PostJournalEntry postEntry, ReverseJournalEntry reverseEntry, ListJournalEntries listEntries,
                         GetTrialBalance trialBalance, GetAccountLedger accountLedger, GetPartnerLedger partnerLedger,
                         GetPeriodLock periodLock, ClosePeriod closePeriod, ReopenPeriod reopenPeriod,
                         GetProfitAndLoss profitAndLoss, GetBalanceSheet balanceSheet, GetCashFlow cashFlow,
                         GetAccountingSummary summary) {
        this.listAccounts = listAccounts;
        this.createAccount = createAccount;
        this.updateAccount = updateAccount;
        this.postEntry = postEntry;
        this.reverseEntry = reverseEntry;
        this.listEntries = listEntries;
        this.trialBalance = trialBalance;
        this.accountLedger = accountLedger;
        this.partnerLedger = partnerLedger;
        this.periodLock = periodLock;
        this.closePeriod = closePeriod;
        this.reopenPeriod = reopenPeriod;
        this.profitAndLoss = profitAndLoss;
        this.balanceSheet = balanceSheet;
        this.cashFlow = cashFlow;
        this.summary = summary;
    }

    record AccountRequest(@NotBlank String code, @NotBlank String name, @NotNull AccountKind type) {}

    record AccountUpdateRequest(String name, Boolean active) {}

    record LineRequest(@NotBlank String accountCode, BigDecimal debit, BigDecimal credit, Long partnerId, String memo) {
        PostEntryCommand.Line toCommand() {
            return new PostEntryCommand.Line(accountCode, debit == null ? BigDecimal.ZERO : debit,
                    credit == null ? BigDecimal.ZERO : credit, partnerId, memo);
        }
    }

    record CloseRequest(@NotNull LocalDate lockDate) {}

    record ReopenRequest(LocalDate lockDate) {}

    record EntryRequest(@NotNull LocalDate date, String reference, String description, @NotEmpty @Valid List<LineRequest> lines) {}

    @GetMapping("/accounts")
    List<AccountDTO> accounts() {
        return listAccounts.handle();
    }

    @PostMapping("/accounts")
    AccountDTO createAccount(@RequestBody @Valid AccountRequest r) {
        return createAccount.handle(new CreateAccountCommand(r.code(), r.name(), r.type()));
    }

    @PutMapping("/accounts/{code}")
    AccountDTO updateAccount(@PathVariable String code, @RequestBody AccountUpdateRequest r) {
        return updateAccount.handle(code, r.name(), r.active());
    }

    @GetMapping("/entries")
    List<JournalEntryDTO> entries() {
        return listEntries.handle();
    }

    @PostMapping("/entries")
    JournalEntryDTO post(@RequestBody @Valid EntryRequest r) {
        return postEntry.handle(new PostEntryCommand(r.date(), r.reference(), r.description(),
                r.lines().stream().map(LineRequest::toCommand).toList()), EntryKind.MANUAL);
    }

    @PostMapping("/entries/{id}/reverse")
    JournalEntryDTO reverse(@PathVariable long id) {
        return reverseEntry.handle(id);
    }

    @GetMapping("/reports/trial-balance")
    TrialBalanceDTO trialBalance() {
        return trialBalance.handle();
    }

    /** Without dates: the current year to date. */
    @GetMapping("/reports/profit-and-loss")
    ProfitAndLossDTO profitAndLoss(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return profitAndLoss.handle(from, to);
    }

    /** Without a date: as of today. */
    @GetMapping("/reports/balance-sheet")
    BalanceSheetDTO balanceSheet(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        return balanceSheet.handle(asOf);
    }

    /** Without dates: the current month to date. */
    @GetMapping("/reports/cash-flow")
    CashFlowDTO cashFlow(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return cashFlow.handle(from, to);
    }

    /** Without dates: the current month to date. */
    @GetMapping("/reports/summary")
    AccountingSummaryDTO summary(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return summary.handle(from, to);
    }

    @GetMapping("/ledger/account/{code}")
    List<LedgerRowDTO> accountLedger(@PathVariable String code) {
        return accountLedger.handle(code);
    }

    @GetMapping("/ledger/partner/{id}")
    List<LedgerRowDTO> partnerLedger(@PathVariable long id) {
        return partnerLedger.handle(id);
    }

    @GetMapping("/periods")
    PeriodLockDTO period() {
        return periodLock.handle();
    }

    @PostMapping("/periods/close")
    PeriodLockDTO closePeriod(@RequestBody @Valid CloseRequest r) {
        return closePeriod.handle(r.lockDate());
    }

    /** Body is optional: without a lockDate every period is reopened. */
    @PostMapping("/periods/reopen")
    PeriodLockDTO reopenPeriod(@RequestBody(required = false) ReopenRequest r) {
        return reopenPeriod.handle(r == null ? null : r.lockDate());
    }
}
