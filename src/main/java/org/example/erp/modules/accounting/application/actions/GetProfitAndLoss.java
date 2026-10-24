package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.contracts.LedgerReadModel;
import org.example.erp.modules.accounting.application.dto.ProfitAndLossDTO;
import org.example.erp.modules.accounting.application.dto.StatementLineDTO;
import org.example.erp.modules.accounting.application.enums.AccountKind;
import org.example.erp.modules.accounting.domain.AccountRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Revenue and expense accounts (cost of goods sold included) over a period; defaults to the current year to date. */
public final class GetProfitAndLoss {
    private final AccountRepository accounts;
    private final LedgerReadModel ledger;
    private final Clock clock;

    public GetProfitAndLoss(AccountRepository accounts, LedgerReadModel ledger, Clock clock) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.clock = clock;
    }

    public ProfitAndLossDTO handle(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now(clock);
        LocalDate start = from != null ? from : end.withDayOfYear(1);
        StatementBalances.ensureOrdered(start, end);
        Map<AccountKind, List<StatementLineDTO>> lines = StatementBalances.byKind(accounts.findAllByCode(), ledger.totalsByAccount(start, end));
        List<StatementLineDTO> revenue = lines.get(AccountKind.REVENUE);
        List<StatementLineDTO> expenses = lines.get(AccountKind.EXPENSE);
        BigDecimal totalRevenue = StatementBalances.sum(revenue);
        BigDecimal totalExpenses = StatementBalances.sum(expenses);
        return new ProfitAndLossDTO(start, end, revenue, totalRevenue, expenses, totalExpenses, totalRevenue.subtract(totalExpenses));
    }
}
