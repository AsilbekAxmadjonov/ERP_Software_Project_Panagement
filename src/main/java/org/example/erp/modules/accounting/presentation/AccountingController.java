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

    AccountingController(ListAccounts listAccounts, CreateAccount createAccount, UpdateAccount updateAccount,
                         PostJournalEntry postEntry, ReverseJournalEntry reverseEntry, ListJournalEntries listEntries,
                         GetTrialBalance trialBalance, GetAccountLedger accountLedger, GetPartnerLedger partnerLedger) {
        this.listAccounts = listAccounts;
        this.createAccount = createAccount;
        this.updateAccount = updateAccount;
        this.postEntry = postEntry;
        this.reverseEntry = reverseEntry;
        this.listEntries = listEntries;
        this.trialBalance = trialBalance;
        this.accountLedger = accountLedger;
        this.partnerLedger = partnerLedger;
    }

    record AccountRequest(@NotBlank String code, @NotBlank String name, @NotNull AccountKind type) {}

    record AccountUpdateRequest(String name, Boolean active) {}

    record LineRequest(@NotBlank String accountCode, BigDecimal debit, BigDecimal credit, Long partnerId, String memo) {
        PostEntryCommand.Line toCommand() {
            return new PostEntryCommand.Line(accountCode, debit == null ? BigDecimal.ZERO : debit,
                    credit == null ? BigDecimal.ZERO : credit, partnerId, memo);
        }
    }

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

    @GetMapping("/ledger/account/{code}")
    List<LedgerRowDTO> accountLedger(@PathVariable String code) {
        return accountLedger.handle(code);
    }

    @GetMapping("/ledger/partner/{id}")
    List<LedgerRowDTO> partnerLedger(@PathVariable long id) {
        return partnerLedger.handle(id);
    }
}
