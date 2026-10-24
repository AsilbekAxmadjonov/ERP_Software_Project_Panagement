package org.example.erp.modules.employees.domain;

import org.example.erp.platform.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeLedgerTest {
    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);
    private static final LocalDate MONTH_END = OCTOBER.atEndOfMonth();

    private static BigDecimal n(String v) {
        return new BigDecimal(v);
    }

    private static Employee aziz() {
        return Employee.restore(7L, "Aziz Karimov", "Baker", null, n("5000000.00"), EmployeeStatus.ACTIVE);
    }

    /** Records entries one after another the way the use case does, giving each saved entry an id. */
    private static final class Account {
        private final Employee employee;
        private final List<EmployeeLedgerEntry> saved = new ArrayList<>();

        Account(Employee employee) {
            this.employee = employee;
        }

        EmployeeLedger ledger() {
            return new EmployeeLedger(employee, saved);
        }

        void record(LedgerEntryType type, LocalDate date, String amount, PaymentMethod method, YearMonth period) {
            EmployeeLedgerEntry e = ledger().record(type, date, n(amount), method, period, null, "accountant");
            saved.add(new EmployeeLedgerEntry((long) saved.size() + 1, e.employeeId(), e.date(), e.type(), e.amount(), e.method(),
                    e.period(), e.description(), e.createdBy()));
        }
    }

    @Test
    void accrualsAndBonusesIncreaseWhatIsOwedAndPaymentsAdvancesAndDeductionsDecreaseIt() {
        Account account = new Account(aziz());
        account.record(LedgerEntryType.SALARY_ACCRUED, MONTH_END, "5000000", null, OCTOBER);
        account.record(LedgerEntryType.BONUS, MONTH_END, "500000", null, null);
        account.record(LedgerEntryType.DEDUCTION, MONTH_END, "200000", null, null);
        account.record(LedgerEntryType.PAYMENT, MONTH_END.plusDays(3), "5300000", PaymentMethod.BANK, null);

        assertEquals(0, BigDecimal.ZERO.compareTo(account.ledger().balance()));

        account.record(LedgerEntryType.ADVANCE, MONTH_END.plusDays(10), "1000000", PaymentMethod.CASH, null);
        assertEquals(0, n("-1000000").compareTo(account.ledger().balance()), "an advance leaves the employee owing the company");
        assertEquals(0, n("5300000").compareTo(account.ledger().balanceBefore(MONTH_END.plusDays(1))));
    }

    @Test
    void aMonthsSalaryIsAccruedOnlyOnce() {
        Account account = new Account(aziz());
        account.record(LedgerEntryType.SALARY_ACCRUED, MONTH_END, "5000000", null, OCTOBER);

        assertThrows(BusinessRuleException.class, () -> account.record(LedgerEntryType.SALARY_ACCRUED, MONTH_END, "5000000", null, OCTOBER));
        account.record(LedgerEntryType.SALARY_ACCRUED, OCTOBER.plusMonths(1).atEndOfMonth(), "5000000", null, OCTOBER.plusMonths(1));
        assertEquals(0, n("10000000").compareTo(account.ledger().balance()));
    }

    @Test
    void paymentsAndDeductionsCannotExceedWhatIsOwed() {
        Account account = new Account(aziz());
        account.record(LedgerEntryType.SALARY_ACCRUED, MONTH_END, "5000000", null, OCTOBER);

        assertThrows(BusinessRuleException.class, () -> account.record(LedgerEntryType.PAYMENT, MONTH_END, "5000000.01", PaymentMethod.CASH, null));
        assertThrows(BusinessRuleException.class, () -> account.record(LedgerEntryType.DEDUCTION, MONTH_END, "6000000", null, null));
        account.record(LedgerEntryType.PAYMENT, MONTH_END, "5000000", PaymentMethod.CASH, null);
        assertThrows(BusinessRuleException.class, () -> account.record(LedgerEntryType.PAYMENT, MONTH_END, "1", PaymentMethod.CASH, null));
    }

    @Test
    void inactiveEmployeesEarnNothingNewButCanStillBePaidWhatTheyAreOwed() {
        Employee employee = aziz();
        Account account = new Account(employee);
        account.record(LedgerEntryType.SALARY_ACCRUED, MONTH_END, "5000000", null, OCTOBER);
        employee.deactivate();

        assertThrows(BusinessRuleException.class, () -> account.record(LedgerEntryType.BONUS, MONTH_END, "100", null, null));
        assertThrows(BusinessRuleException.class, () -> account.record(LedgerEntryType.ADVANCE, MONTH_END, "100", PaymentMethod.CASH, null));
        assertThrows(BusinessRuleException.class,
                () -> account.record(LedgerEntryType.SALARY_ACCRUED, MONTH_END, "100", null, OCTOBER.plusMonths(1)));
        account.record(LedgerEntryType.PAYMENT, MONTH_END, "5000000", PaymentMethod.BANK, null);
        assertEquals(0, BigDecimal.ZERO.compareTo(account.ledger().balance()));
    }

    @Test
    void entriesMustBePositiveAndCashMovementsNeedAMethod() {
        assertThrows(BusinessRuleException.class,
                () -> new EmployeeLedgerEntry(null, 7, MONTH_END, LedgerEntryType.BONUS, BigDecimal.ZERO, null, null, null, "a"));
        assertThrows(BusinessRuleException.class,
                () -> new EmployeeLedgerEntry(null, 7, MONTH_END, LedgerEntryType.PAYMENT, BigDecimal.TEN, null, null, null, "a"));
        assertThrows(BusinessRuleException.class,
                () -> new EmployeeLedgerEntry(null, 7, MONTH_END, LedgerEntryType.BONUS, BigDecimal.TEN, PaymentMethod.CASH, null, null, "a"));
        assertThrows(BusinessRuleException.class,
                () -> new EmployeeLedgerEntry(null, 7, MONTH_END, LedgerEntryType.SALARY_ACCRUED, BigDecimal.TEN, null, null, null, "a"));
    }

    @Test
    void anEmployeeNeedsANameAndANonNegativeSalary() {
        assertThrows(BusinessRuleException.class, () -> Employee.hire(" ", null, null, BigDecimal.ONE));
        assertThrows(BusinessRuleException.class, () -> Employee.hire("Aziz", null, null, n("-1")));
        Employee hired = Employee.hire(" Aziz ", " ", null, BigDecimal.ZERO);
        assertEquals("Aziz", hired.getFullName());
        assertNull(hired.getPosition());
        assertTrue(hired.isActive());
    }
}
