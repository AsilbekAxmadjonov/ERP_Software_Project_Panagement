package org.example.erp;

import org.example.erp.modules.accounting.application.actions.GetTrialBalance;
import org.example.erp.modules.accounting.application.dto.TrialBalanceDTO;
import org.example.erp.modules.employees.application.actions.*;
import org.example.erp.modules.employees.application.commands.RecordLedgerEntryCommand;
import org.example.erp.modules.employees.application.commands.SaveEmployeeCommand;
import org.example.erp.modules.employees.application.dto.EmployeeDTO;
import org.example.erp.modules.employees.application.dto.EmployeeLedgerRowDTO;
import org.example.erp.modules.employees.application.dto.EmployeeStatementDTO;
import org.example.erp.modules.employees.application.enums.EmployeeState;
import org.example.erp.modules.employees.application.enums.LedgerEntryKind;
import org.example.erp.modules.employees.application.enums.PaymentChannel;
import org.example.erp.platform.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Accrue a month's salary, pay part of it, give a bonus and a deduction, then an advance, and read the statement. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:payrollflow;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@WithMockUser(username = "accountant", roles = "ACCOUNTANT")
class EmployeesFlowIntegrationTest {
    private static final YearMonth SEPTEMBER = YearMonth.of(2026, 9);
    private static final LocalDate PAYDAY = SEPTEMBER.atEndOfMonth();

    @Autowired CreateEmployee createEmployee;
    @Autowired UpdateEmployee updateEmployee;
    @Autowired FindEmployee findEmployee;
    @Autowired RecordEmployeeLedgerEntry recordEntry;
    @Autowired AccrueMonthlySalaries accrueSalaries;
    @Autowired GetEmployeeLedger getLedger;
    @Autowired GetEmployeeStatement getStatement;
    @Autowired GetTrialBalance trialBalance;
    @Autowired MockMvc mvc;

    private static BigDecimal n(String v) {
        return new BigDecimal(v);
    }

    private static BigDecimal balanceOf(TrialBalanceDTO tb, String code) {
        return tb.rows().stream().filter(r -> r.code().equals(code)).findFirst()
                .map(r -> r.debit().subtract(r.credit())).orElse(BigDecimal.ZERO);
    }

    private static BigDecimal change(TrialBalanceDTO after, TrialBalanceDTO before, String code) {
        return balanceOf(after, code).subtract(balanceOf(before, code));
    }

    private static void assertBalanced(TrialBalanceDTO tb) {
        assertEquals(0, tb.totalDebit().compareTo(tb.totalCredit()), "ledger must stay balanced");
    }

    private EmployeeLedgerRowDTO record(long employeeId, LedgerEntryKind type, LocalDate date, String amount, PaymentChannel method) {
        return recordEntry.handle(new RecordLedgerEntryCommand(employeeId, type, date, amount == null ? null : n(amount), method, null, null));
    }

    @Test
    void salaryAccrualAndPaymentsKeepSalariesPayableEqualToWhatStaffAreOwed() {
        EmployeeDTO baker = createEmployee.handle(new SaveEmployeeCommand("Aziz Karimov", "Baker", "+998901112233", n("5000000"), null));
        assertEquals(EmployeeState.ACTIVE, baker.status());
        TrialBalanceDTO before = trialBalance.handle();

        EmployeeLedgerRowDTO accrued = record(baker.id(), LedgerEntryKind.SALARY_ACCRUED, PAYDAY, null, null);
        assertEquals(0, n("5000000").compareTo(accrued.earned()), "the monthly salary is accrued by default");
        assertEquals(SEPTEMBER.toString(), accrued.period());
        record(baker.id(), LedgerEntryKind.PAYMENT, PAYDAY.plusDays(2), "3000000", PaymentChannel.BANK);

        TrialBalanceDTO afterPayment = trialBalance.handle();
        assertBalanced(afterPayment);
        assertEquals(0, n("5000000").compareTo(change(afterPayment, before, "6500")), "Dr salary expense");
        assertEquals(0, n("-2000000").compareTo(change(afterPayment, before, "2100")), "Cr salaries payable: 5,000,000 - 3,000,000");
        assertEquals(0, n("-3000000").compareTo(change(afterPayment, before, "1100")), "Cr bank");
        assertEquals(0, n("2000000").compareTo(findEmployee.handle(baker.id()).balance()));

        record(baker.id(), LedgerEntryKind.BONUS, PAYDAY.plusDays(5), "500000", null);
        record(baker.id(), LedgerEntryKind.DEDUCTION, PAYDAY.plusDays(5), "100000", null);
        assertThrows(BusinessRuleException.class, () -> record(baker.id(), LedgerEntryKind.PAYMENT, PAYDAY.plusDays(6), "2400000.01", PaymentChannel.CASH));
        record(baker.id(), LedgerEntryKind.PAYMENT, PAYDAY.plusDays(6), "2400000", PaymentChannel.CASH);
        record(baker.id(), LedgerEntryKind.ADVANCE, PAYDAY.plusDays(15), "1000000", PaymentChannel.CASH);

        TrialBalanceDTO after = trialBalance.handle();
        assertBalanced(after);
        assertEquals(0, n("5400000").compareTo(change(after, before, "6500")), "salary + bonus - deduction");
        assertEquals(0, n("1000000").compareTo(change(after, before, "2100")), "the advance leaves a debit on salaries payable");
        assertEquals(0, n("-3400000").compareTo(change(after, before, "1000")), "cash paid: 2,400,000 + 1,000,000 advance");
        assertEquals(0, n("-1000000").compareTo(findEmployee.handle(baker.id()).balance()), "the employee owes the advance back");

        List<EmployeeLedgerRowDTO> ledger = getLedger.handle(baker.id());
        assertEquals(6, ledger.size());
        assertEquals(0, n("-1000000").compareTo(ledger.getLast().balance()));

        EmployeeStatementDTO statement = getStatement.handle(baker.id(), PAYDAY.plusDays(1), PAYDAY.plusDays(10));
        assertEquals(0, n("5000000").compareTo(statement.opening()));
        assertEquals(4, statement.rows().size());
        assertEquals(0, n("500000").compareTo(statement.totalEarned()));
        assertEquals(0, n("5500000").compareTo(statement.totalSettled()));
        assertEquals(0, BigDecimal.ZERO.compareTo(statement.closing()));
        assertEquals(0, statement.closing().compareTo(statement.rows().getLast().balance()));
    }

    @Test
    void aPayrollRunAccruesEachActiveEmployeeOncePerMonth() {
        EmployeeDTO driver = createEmployee.handle(new SaveEmployeeCommand("Bekzod Aliyev", "Driver", null, n("4000000"), null));
        EmployeeDTO former = createEmployee.handle(new SaveEmployeeCommand("Dilnoza Rustamova", "Cashier", null, n("3500000"), null));
        updateEmployee.handle(former.id(), new SaveEmployeeCommand("Dilnoza Rustamova", "Cashier", null, n("3500000"), EmployeeState.INACTIVE));
        YearMonth august = YearMonth.of(2026, 8);

        List<EmployeeLedgerRowDTO> run = accrueSalaries.handle(august, august.atEndOfMonth());
        assertTrue(run.stream().anyMatch(r -> r.employeeId() == driver.id()));
        assertTrue(run.stream().noneMatch(r -> r.employeeId() == former.id()), "inactive employees are skipped");
        assertTrue(accrueSalaries.handle(august, august.atEndOfMonth()).isEmpty(), "a second run accrues nothing");
        assertThrows(BusinessRuleException.class, () -> recordEntry.handle(new RecordLedgerEntryCommand(driver.id(),
                LedgerEntryKind.SALARY_ACCRUED, august.atEndOfMonth(), null, null, august, null)));
    }

    @Test
    void managersCanReadButNotPayEmployees() throws Exception {
        EmployeeDTO cook = createEmployee.handle(new SaveEmployeeCommand("Olim Nazarov", "Cook", null, n("4500000"), null));
        mvc.perform(get("/api/employees/" + cook.id()).with(user("manager").roles("MANAGER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Olim Nazarov"));
        mvc.perform(post("/api/employees/" + cook.id() + "/ledger").with(user("manager").roles("MANAGER")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"BONUS\",\"amount\":100}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/employees/" + cook.id() + "/ledger").with(user("ceo").roles("CEO")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"SALARY_ACCRUED\",\"period\":\"2026-07\",\"date\":\"2026-07-31\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.period").value("2026-07")).andExpect(jsonPath("$.reference").value(org.hamcrest.Matchers.startsWith("SAL-")));
    }
}
