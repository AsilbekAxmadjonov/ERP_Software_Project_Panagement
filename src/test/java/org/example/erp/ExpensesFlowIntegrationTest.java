package org.example.erp;

import org.example.erp.modules.accounting.application.actions.ClosePeriod;
import org.example.erp.modules.accounting.application.actions.GetTrialBalance;
import org.example.erp.modules.accounting.application.dto.TrialBalanceDTO;
import org.example.erp.modules.expenses.application.actions.*;
import org.example.erp.modules.expenses.application.commands.CreateCategoryCommand;
import org.example.erp.modules.expenses.application.commands.RecordCashEntryCommand;
import org.example.erp.modules.expenses.application.dto.CashEntryDTO;
import org.example.erp.modules.expenses.application.dto.CategoryDTO;
import org.example.erp.modules.expenses.application.dto.CategoryReportDTO;
import org.example.erp.modules.expenses.application.enums.EntryState;
import org.example.erp.modules.expenses.application.enums.EntryType;
import org.example.erp.modules.expenses.application.enums.PaymentChannel;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pay rent in cash and transport by bank, receive interest, cancel the rent, and report by category. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:expenseflow;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@WithMockUser(username = "accountant", roles = "ACCOUNTANT")
class ExpensesFlowIntegrationTest {
    @Autowired ListCategories listCategories;
    @Autowired CreateCategory createCategory;
    @Autowired RecordCashEntry recordEntry;
    @Autowired CancelCashEntry cancelEntry;
    @Autowired ListCashEntries listEntries;
    @Autowired GetCategoryReport report;
    @Autowired GetTrialBalance trialBalance;
    @Autowired ClosePeriod closePeriod;
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

    private CategoryDTO category(EntryType type, String name) {
        return listCategories.handle(type).stream().filter(c -> c.name().equals(name)).findFirst().orElseThrow();
    }

    private static void assertBalanced(TrialBalanceDTO tb) {
        assertEquals(0, tb.totalDebit().compareTo(tb.totalCredit()), "ledger must stay balanced");
    }

    @Test
    void expensesAndIncomePostToTheirAccountsAndCancellationReversesThem() {
        CategoryDTO rent = category(EntryType.EXPENSE, "Rent");
        CategoryDTO transport = category(EntryType.EXPENSE, "Transport");
        CategoryDTO interest = category(EntryType.INCOME, "Interest");
        assertEquals("6100", rent.accountCode());
        assertEquals("4100", interest.accountCode());
        TrialBalanceDTO before = trialBalance.handle();

        CashEntryDTO rentPaid = recordEntry.handle(new RecordCashEntryCommand(EntryType.EXPENSE, rent.id(), null, n("3000000"), PaymentChannel.CASH, "October"));
        recordEntry.handle(new RecordCashEntryCommand(EntryType.EXPENSE, transport.id(), null, n("200000"), PaymentChannel.BANK, null));
        recordEntry.handle(new RecordCashEntryCommand(EntryType.INCOME, interest.id(), null, n("500000"), PaymentChannel.BANK, null));
        assertTrue(rentPaid.number().matches("EXP-\\d{5}"));

        TrialBalanceDTO posted = trialBalance.handle();
        assertBalanced(posted);
        assertEquals(0, n("3000000").compareTo(change(posted, before, "6100")), "Dr rent expense");
        assertEquals(0, n("-3000000").compareTo(change(posted, before, "1000")), "Cr cash");
        assertEquals(0, n("200000").compareTo(change(posted, before, "6300")), "Dr transport expense");
        assertEquals(0, n("300000").compareTo(change(posted, before, "1100")), "bank: +500,000 interest - 200,000 transport");
        assertEquals(0, n("-500000").compareTo(change(posted, before, "4100")), "Cr other income");

        CashEntryDTO cancelled = cancelEntry.handle(rentPaid.id(), null);
        assertEquals(EntryState.CANCELLED, cancelled.status());
        assertThrows(BusinessRuleException.class, () -> cancelEntry.handle(rentPaid.id(), null));
        TrialBalanceDTO reversed = trialBalance.handle();
        assertBalanced(reversed);
        assertEquals(0, BigDecimal.ZERO.compareTo(change(reversed, before, "6100")));
        assertEquals(0, BigDecimal.ZERO.compareTo(change(reversed, before, "1000")));

        LocalDate today = LocalDate.now();
        CategoryReportDTO byCategory = report.handle(today.minusDays(1), today.plusDays(1));
        assertEquals(1, byCategory.expenses().size(), "the cancelled rent is left out");
        assertEquals("Transport", byCategory.expenses().getFirst().categoryName());
        assertEquals(0, n("200000").compareTo(byCategory.totalExpenses()));
        assertEquals(0, n("500000").compareTo(byCategory.totalIncome()));
        assertEquals(0, n("300000").compareTo(byCategory.net()));
        assertTrue(report.handle(today.plusDays(1), today.plusDays(2)).expenses().isEmpty());
    }

    @Test
    void categoriesMustMatchTheEntryTypeAndPostToAnAccountOfTheRightNature() {
        CategoryDTO interest = category(EntryType.INCOME, "Interest");
        assertThrows(BusinessRuleException.class, () -> recordEntry.handle(
                new RecordCashEntryCommand(EntryType.EXPENSE, interest.id(), null, n("10"), PaymentChannel.CASH, null)));

        assertThrows(BusinessRuleException.class, () -> createCategory.handle(new CreateCategoryCommand("Marketing", EntryType.EXPENSE, "4100")),
                "an expense category cannot post to a revenue account");
        assertThrows(BusinessRuleException.class, () -> createCategory.handle(new CreateCategoryCommand("Rent", EntryType.EXPENSE, "6100")),
                "names are unique per type");
        CategoryDTO marketing = createCategory.handle(new CreateCategoryCommand("Marketing", EntryType.EXPENSE, "6900"));
        assertTrue(marketing.active());
    }

    @Test
    void anExpenseInAClosedPeriodIsRejectedTogetherWithItsLedgerEntry() {
        CategoryDTO utilities = category(EntryType.EXPENSE, "Utilities");
        closePeriod.handle(LocalDate.of(2020, 1, 31));
        int recorded = listEntries.handle(EntryType.EXPENSE).size();

        assertThrows(BusinessRuleException.class, () -> recordEntry.handle(new RecordCashEntryCommand(EntryType.EXPENSE, utilities.id(),
                LocalDate.of(2020, 1, 15), n("90000"), PaymentChannel.CASH, null)));
        assertEquals(recorded, listEntries.handle(EntryType.EXPENSE).size(), "nothing is saved when the posting fails");
    }

    @Test
    void managersCanReadButNotRecordExpenses() throws Exception {
        mvc.perform(get("/api/expenses/categories").with(user("manager").roles("MANAGER"))).andExpect(status().isOk());
        mvc.perform(post("/api/expenses/entries").with(user("manager").roles("MANAGER")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"EXPENSE\",\"categoryId\":1,\"amount\":10,\"method\":\"CASH\"}")).andExpect(status().isForbidden());
    }
}
