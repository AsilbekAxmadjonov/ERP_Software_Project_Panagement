package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.contracts.LedgerReadModel;
import org.example.erp.modules.accounting.application.contracts.LedgerReadModel.AccountTotals;
import org.example.erp.modules.accounting.application.contracts.LedgerReadModel.MovementLine;
import org.example.erp.modules.accounting.application.dto.CashFlowDTO;
import org.example.erp.modules.accounting.application.enums.EntryKind;
import org.example.erp.modules.accounting.application.enums.LedgerRole;
import org.example.erp.modules.accounting.domain.Account;
import org.example.erp.modules.accounting.domain.AccountRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

/**
 * Cash (Kassa) and bank movements over a period (the current month to date by default): opening and closing balances,
 * inflows and outflows per account, grouped by the origin of the journal entry, per day and per movement.
 */
public final class GetCashFlow {
    private static final List<LedgerRole> CASH_ROLES = List.of(LedgerRole.CASH, LedgerRole.BANK);
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final AccountRepository accounts;
    private final LedgerReadModel ledger;
    private final Clock clock;

    public GetCashFlow(AccountRepository accounts, LedgerReadModel ledger, Clock clock) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.clock = clock;
    }

    public CashFlowDTO handle(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now(clock);
        LocalDate start = from != null ? from : end.withDayOfMonth(1);
        StatementBalances.ensureOrdered(start, end);

        List<String> codes = CASH_ROLES.stream().map(LedgerRole::code).toList();
        Map<String, String> names = new HashMap<>();
        for (LedgerRole role : CASH_ROLES)
            names.put(role.code(), accounts.findByCode(role.code()).map(Account::getName).orElse(role.defaultName()));

        Map<String, BigDecimal> opening = new LinkedHashMap<>();
        for (String code : codes) opening.put(code, ZERO);
        for (AccountTotals t : ledger.totalsByAccount(null, start.minusDays(1)))
            if (opening.containsKey(t.accountCode())) opening.put(t.accountCode(), t.debit().subtract(t.credit()));

        Map<String, BigDecimal> inflowByAccount = new HashMap<>();
        Map<String, BigDecimal> outflowByAccount = new HashMap<>();
        Map<EntryKind, OriginSum> inflowByOrigin = new EnumMap<>(EntryKind.class);
        Map<EntryKind, OriginSum> outflowByOrigin = new EnumMap<>(EntryKind.class);
        Map<LocalDate, BigDecimal[]> byDay = new TreeMap<>();
        List<CashFlowDTO.Movement> movements = new ArrayList<>();
        BigDecimal balance = opening.values().stream().reduce(ZERO, BigDecimal::add);
        BigDecimal openingTotal = balance;

        for (MovementLine l : ledger.movementLines(codes, start, end)) {
            BigDecimal net = l.debit().subtract(l.credit());
            BigDecimal in = net.signum() > 0 ? net : ZERO;
            BigDecimal out = net.signum() < 0 ? net.negate() : ZERO;
            balance = balance.add(net);
            inflowByAccount.merge(l.accountCode(), in, BigDecimal::add);
            outflowByAccount.merge(l.accountCode(), out, BigDecimal::add);
            if (in.signum() > 0) inflowByOrigin.computeIfAbsent(l.origin(), k -> new OriginSum()).add(in, l.entryId());
            if (out.signum() > 0) outflowByOrigin.computeIfAbsent(l.origin(), k -> new OriginSum()).add(out, l.entryId());
            BigDecimal[] day = byDay.computeIfAbsent(l.date(), d -> new BigDecimal[]{ZERO, ZERO});
            day[0] = day[0].add(in);
            day[1] = day[1].add(out);
            movements.add(new CashFlowDTO.Movement(l.entryId(), l.date(), l.reference(), l.description(), l.origin(),
                    l.accountCode(), names.get(l.accountCode()), in, out, balance));
        }

        List<CashFlowDTO.AccountFlow> accountFlows = new ArrayList<>();
        BigDecimal totalIn = ZERO;
        BigDecimal totalOut = ZERO;
        for (String code : codes) {
            BigDecimal in = inflowByAccount.getOrDefault(code, ZERO);
            BigDecimal out = outflowByAccount.getOrDefault(code, ZERO);
            accountFlows.add(new CashFlowDTO.AccountFlow(code, names.get(code), opening.get(code), in, out, opening.get(code).add(in).subtract(out)));
            totalIn = totalIn.add(in);
            totalOut = totalOut.add(out);
        }

        List<CashFlowDTO.Day> days = new ArrayList<>();
        BigDecimal running = openingTotal;
        for (Map.Entry<LocalDate, BigDecimal[]> d : byDay.entrySet()) {
            running = running.add(d.getValue()[0]).subtract(d.getValue()[1]);
            days.add(new CashFlowDTO.Day(d.getKey(), d.getValue()[0], d.getValue()[1], running));
        }

        return new CashFlowDTO(start, end, accountFlows, openingTotal, totalIn, totalOut, openingTotal.add(totalIn).subtract(totalOut),
                toOriginTotals(inflowByOrigin), toOriginTotals(outflowByOrigin), days, movements);
    }

    private static List<CashFlowDTO.OriginTotal> toOriginTotals(Map<EntryKind, OriginSum> sums) {
        return sums.entrySet().stream()
                .map(e -> new CashFlowDTO.OriginTotal(e.getKey(), e.getValue().amount, e.getValue().entries.size()))
                .sorted(Comparator.comparing(CashFlowDTO.OriginTotal::amount).reversed())
                .toList();
    }

    private static final class OriginSum {
        private BigDecimal amount = ZERO;
        private final Set<Long> entries = new HashSet<>();

        void add(BigDecimal value, long entryId) {
            amount = amount.add(value);
            entries.add(entryId);
        }
    }
}
