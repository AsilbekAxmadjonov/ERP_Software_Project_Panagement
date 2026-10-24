package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.contracts.LedgerReadModel;
import org.example.erp.modules.accounting.application.dto.BalanceSheetDTO;
import org.example.erp.modules.accounting.application.dto.StatementLineDTO;
import org.example.erp.modules.accounting.application.enums.AccountKind;
import org.example.erp.modules.accounting.domain.AccountRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Assets, liabilities and equity as of a date (today by default). Revenue and expenses are never closed into an
 * equity account by a posting, so their difference up to that date is shown as current earnings within equity.
 */
public final class GetBalanceSheet {
    private final AccountRepository accounts;
    private final LedgerReadModel ledger;
    private final Clock clock;

    public GetBalanceSheet(AccountRepository accounts, LedgerReadModel ledger, Clock clock) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.clock = clock;
    }

    public BalanceSheetDTO handle(LocalDate asOf) {
        LocalDate date = asOf != null ? asOf : LocalDate.now(clock);
        Map<AccountKind, List<StatementLineDTO>> lines = StatementBalances.byKind(accounts.findAllByCode(), ledger.totalsByAccount(null, date));
        BigDecimal totalAssets = StatementBalances.sum(lines.get(AccountKind.ASSET));
        BigDecimal totalLiabilities = StatementBalances.sum(lines.get(AccountKind.LIABILITY));
        BigDecimal currentEarnings = StatementBalances.sum(lines.get(AccountKind.REVENUE))
                .subtract(StatementBalances.sum(lines.get(AccountKind.EXPENSE)));
        BigDecimal totalEquity = StatementBalances.sum(lines.get(AccountKind.EQUITY)).add(currentEarnings);
        BigDecimal liabilitiesAndEquity = totalLiabilities.add(totalEquity);
        return new BalanceSheetDTO(date, lines.get(AccountKind.ASSET), totalAssets, lines.get(AccountKind.LIABILITY), totalLiabilities,
                lines.get(AccountKind.EQUITY), currentEarnings, totalEquity, liabilitiesAndEquity,
                totalAssets.compareTo(liabilitiesAndEquity) == 0);
    }
}
