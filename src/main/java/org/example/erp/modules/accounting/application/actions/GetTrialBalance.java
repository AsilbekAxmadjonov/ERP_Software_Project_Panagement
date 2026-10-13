package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.contracts.LedgerReadModel;
import org.example.erp.modules.accounting.application.dto.TrialBalanceDTO;
import org.example.erp.modules.accounting.application.enums.AccountKind;
import org.example.erp.modules.accounting.domain.Account;
import org.example.erp.modules.accounting.domain.AccountRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GetTrialBalance {
    private final AccountRepository accounts;
    private final LedgerReadModel ledger;

    public GetTrialBalance(AccountRepository accounts, LedgerReadModel ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    public TrialBalanceDTO handle() {
        Map<String, LedgerReadModel.AccountTotals> totals = ledger.totalsByAccount().stream()
                .collect(Collectors.toMap(LedgerReadModel.AccountTotals::accountCode, Function.identity()));
        List<TrialBalanceDTO.Row> rows = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (Account a : accounts.findAllByCode()) {
            LedgerReadModel.AccountTotals t = totals.get(a.getCode());
            if (t == null) continue;
            BigDecimal net = t.debit().subtract(t.credit());
            BigDecimal debit = net.signum() > 0 ? net : BigDecimal.ZERO;
            BigDecimal credit = net.signum() < 0 ? net.negate() : BigDecimal.ZERO;
            rows.add(new TrialBalanceDTO.Row(a.getCode(), a.getName(), AccountKind.valueOf(a.getType().name()), debit, credit));
            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);
        }
        return new TrialBalanceDTO(rows, totalDebit, totalCredit);
    }
}
