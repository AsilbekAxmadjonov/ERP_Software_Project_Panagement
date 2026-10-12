package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.contracts.LedgerReadModel;
import org.example.erp.modules.accounting.application.dto.LedgerRowDTO;
import org.example.erp.modules.accounting.domain.Account;
import org.example.erp.modules.accounting.domain.AccountRepository;
import org.example.erp.platform.NotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Company ledger: running balance of one account, on the side where that account type grows. */
public final class GetAccountLedger {
    private final AccountRepository accounts;
    private final LedgerReadModel ledger;

    public GetAccountLedger(AccountRepository accounts, LedgerReadModel ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    public List<LedgerRowDTO> handle(String accountCode) {
        Account account = accounts.findByCode(accountCode).orElseThrow(() -> new NotFoundException("Account not found"));
        boolean debitNormal = account.getType().isDebitNormal();
        BigDecimal balance = BigDecimal.ZERO;
        List<LedgerRowDTO> rows = new ArrayList<>();
        for (LedgerReadModel.LedgerLine l : ledger.accountLines(accountCode)) {
            balance = balance.add(debitNormal ? l.debit().subtract(l.credit()) : l.credit().subtract(l.debit()));
            rows.add(new LedgerRowDTO(l.entryId(), l.date(), l.reference(), l.description(), l.debit(), l.credit(), balance));
        }
        return rows;
    }
}
