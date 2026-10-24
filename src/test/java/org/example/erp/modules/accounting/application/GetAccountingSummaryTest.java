package org.example.erp.modules.accounting.application;

import org.example.erp.modules.accounting.application.actions.GetAccountingSummary;
import org.example.erp.modules.accounting.application.contracts.LedgerReadModel;
import org.example.erp.modules.accounting.application.dto.AccountingSummaryDTO;
import org.example.erp.modules.accounting.domain.Account;
import org.example.erp.modules.accounting.domain.AccountRepository;
import org.example.erp.modules.accounting.domain.AccountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetAccountingSummaryTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 25);

    private record Posting(LocalDate date, String account, String debit, String credit) {}

    private static final List<Account> CHART = List.of(
            Account.restore(1L, "1000", "Cash", AccountType.ASSET, true),
            Account.restore(2L, "1100", "Bank", AccountType.ASSET, true),
            Account.restore(3L, "3000", "Equity", AccountType.EQUITY, true),
            Account.restore(4L, "4000", "Sales", AccountType.REVENUE, true),
            Account.restore(5L, "4100", "Other income", AccountType.REVENUE, true),
            Account.restore(6L, "5000", "COGS", AccountType.EXPENSE, true),
            Account.restore(7L, "5300", "Salaries", AccountType.EXPENSE, true));

    private static final List<Posting> POSTINGS = List.of(
            new Posting(LocalDate.of(2026, 9, 30), "1000", "500", "0"),
            new Posting(LocalDate.of(2026, 9, 30), "3000", "0", "500"),
            new Posting(LocalDate.of(2026, 9, 30), "4000", "0", "999"),
            new Posting(LocalDate.of(2026, 9, 30), "1100", "999", "0"),
            new Posting(LocalDate.of(2026, 10, 3), "1000", "300", "0"),
            new Posting(LocalDate.of(2026, 10, 3), "4000", "0", "250"),
            new Posting(LocalDate.of(2026, 10, 3), "4100", "0", "50"),
            new Posting(LocalDate.of(2026, 10, 10), "5000", "120", "0"),
            new Posting(LocalDate.of(2026, 10, 10), "5300", "80", "0"),
            new Posting(LocalDate.of(2026, 10, 10), "1100", "0", "200"));

    private final GetAccountingSummary summary = new GetAccountingSummary(new Chart(), new Ledger(),
            Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC));

    @Test
    void defaultsToTheCurrentMonthAndSumsEveryRevenueAndExpenseAccount() {
        AccountingSummaryDTO s = summary.handle(null, null);

        assertEquals(LocalDate.of(2026, 10, 1), s.from());
        assertEquals(TODAY, s.to());
        assertEquals(0, new BigDecimal("300").compareTo(s.revenue()), "other income counts as revenue");
        assertEquals(0, new BigDecimal("200").compareTo(s.expenses()), "salaries count as an expense");
        assertEquals(0, new BigDecimal("100").compareTo(s.profit()));
        assertEquals(0, new BigDecimal("800").compareTo(s.cash()), "cash is the balance to date, not the period movement");
        assertEquals(0, new BigDecimal("799").compareTo(s.bank()));
    }

    @Test
    void aYearRangeIncludesEarlierMonths() {
        AccountingSummaryDTO s = summary.handle(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(0, new BigDecimal("1299").compareTo(s.revenue()));
        assertEquals(0, new BigDecimal("1099").compareTo(s.profit()));
    }

    private static final class Chart implements AccountRepository {
        @Override
        public Optional<Account> findByCode(String code) {
            return CHART.stream().filter(a -> a.getCode().equals(code)).findFirst();
        }

        @Override
        public List<Account> findAllByCode() {
            return CHART;
        }

        @Override
        public long count() {
            return CHART.size();
        }

        @Override
        public Account save(Account account) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class Ledger implements LedgerReadModel {
        @Override
        public List<AccountTotals> totalsByAccount() {
            return totalsByAccount(null, null);
        }

        @Override
        public List<AccountTotals> totalsByAccount(LocalDate from, LocalDate to) {
            Map<String, BigDecimal[]> sums = new TreeMap<>();
            for (Posting p : POSTINGS) {
                if ((from != null && p.date().isBefore(from)) || (to != null && p.date().isAfter(to))) continue;
                BigDecimal[] s = sums.computeIfAbsent(p.account(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                s[0] = s[0].add(new BigDecimal(p.debit()));
                s[1] = s[1].add(new BigDecimal(p.credit()));
            }
            return sums.entrySet().stream().map(e -> new AccountTotals(e.getKey(), e.getValue()[0], e.getValue()[1])).toList();
        }

        @Override
        public List<LedgerLine> accountLines(String accountCode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<MovementLine> movementLines(Collection<String> accountCodes, LocalDate from, LocalDate to) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<LedgerLine> partnerLines(long partnerId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigDecimal partnerBalance(long partnerId, String accountCode) {
            throw new UnsupportedOperationException();
        }
    }
}
