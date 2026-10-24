package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.contracts.LedgerReadModel;
import org.example.erp.modules.accounting.application.contracts.LedgerReadModel.AccountTotals;
import org.example.erp.modules.accounting.application.dto.AccountingSummaryDTO;
import org.example.erp.modules.accounting.application.dto.StatementLineDTO;
import org.example.erp.modules.accounting.application.enums.AccountKind;
import org.example.erp.modules.accounting.application.enums.LedgerRole;
import org.example.erp.modules.accounting.domain.AccountRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Key figures for the accounting dashboard; the period defaults to the current month to date. */
public final class GetAccountingSummary {
    private final AccountRepository accounts;
    private final LedgerReadModel ledger;
    private final Clock clock;

    public GetAccountingSummary(AccountRepository accounts, LedgerReadModel ledger, Clock clock) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.clock = clock;
    }

    public AccountingSummaryDTO handle(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now(clock);
        LocalDate start = from != null ? from : end.withDayOfMonth(1);
        StatementBalances.ensureOrdered(start, end);
        Map<AccountKind, List<StatementLineDTO>> period = StatementBalances.byKind(accounts.findAllByCode(), ledger.totalsByAccount(start, end));
        BigDecimal revenue = StatementBalances.sum(period.get(AccountKind.REVENUE));
        BigDecimal expenses = StatementBalances.sum(period.get(AccountKind.EXPENSE));
        List<AccountTotals> toDate = ledger.totalsByAccount(null, end);
        return new AccountingSummaryDTO(start, end, balance(toDate, LedgerRole.CASH), balance(toDate, LedgerRole.BANK),
                revenue, expenses, revenue.subtract(expenses));
    }

    private static BigDecimal balance(List<AccountTotals> totals, LedgerRole role) {
        return totals.stream().filter(t -> t.accountCode().equals(role.code()))
                .map(t -> t.debit().subtract(t.credit()))
                .findFirst().orElse(BigDecimal.ZERO.setScale(2));
    }
}
