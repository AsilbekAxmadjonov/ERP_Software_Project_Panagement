package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.contracts.LedgerReadModel.AccountTotals;
import org.example.erp.modules.accounting.application.dto.StatementLineDTO;
import org.example.erp.modules.accounting.application.enums.AccountKind;
import org.example.erp.modules.accounting.domain.Account;
import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Groups account balances by account kind, so financial statements pick up every account of a kind (including ones
 * other modules add later) without a hardcoded list. Each balance is on the side where its kind grows.
 */
final class StatementBalances {
    private StatementBalances() {}

    static Map<AccountKind, List<StatementLineDTO>> byKind(List<Account> chart, List<AccountTotals> totals) {
        Map<String, AccountTotals> byCode = totals.stream().collect(Collectors.toMap(AccountTotals::accountCode, Function.identity()));
        Map<AccountKind, List<StatementLineDTO>> lines = new EnumMap<>(AccountKind.class);
        for (AccountKind kind : AccountKind.values()) lines.put(kind, new ArrayList<>());
        for (Account account : chart) {
            AccountTotals t = byCode.get(account.getCode());
            if (t == null) continue;
            BigDecimal amount = account.getType().isDebitNormal() ? t.debit().subtract(t.credit()) : t.credit().subtract(t.debit());
            if (amount.signum() == 0) continue;
            AccountKind kind = AccountKind.valueOf(account.getType().name());
            lines.get(kind).add(new StatementLineDTO(account.getCode(), account.getName(), kind, amount));
        }
        return lines;
    }

    static BigDecimal sum(List<StatementLineDTO> lines) {
        return lines.stream().map(StatementLineDTO::amount).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    static void ensureOrdered(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) throw new BusinessRuleException("The start date must not be after the end date");
    }
}
