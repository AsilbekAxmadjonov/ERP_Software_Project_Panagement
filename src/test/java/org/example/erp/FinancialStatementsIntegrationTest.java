package org.example.erp;

import org.example.erp.modules.accounting.application.actions.CreateAccount;
import org.example.erp.modules.accounting.application.actions.GetBalanceSheet;
import org.example.erp.modules.accounting.application.actions.GetCashFlow;
import org.example.erp.modules.accounting.application.actions.GetProfitAndLoss;
import org.example.erp.modules.accounting.application.actions.PostJournalEntry;
import org.example.erp.modules.accounting.application.commands.CreateAccountCommand;
import org.example.erp.modules.accounting.application.commands.PostEntryCommand;
import org.example.erp.modules.accounting.application.commands.PostEntryCommand.Line;
import org.example.erp.modules.accounting.application.dto.BalanceSheetDTO;
import org.example.erp.modules.accounting.application.dto.CashFlowDTO;
import org.example.erp.modules.accounting.application.dto.ProfitAndLossDTO;
import org.example.erp.modules.accounting.application.dto.StatementLineDTO;
import org.example.erp.modules.accounting.application.enums.AccountKind;
import org.example.erp.modules.accounting.application.enums.EntryKind;
import org.example.erp.modules.accounting.application.enums.LedgerRole;
import org.example.erp.modules.inventory.application.actions.CreateProduct;
import org.example.erp.modules.inventory.application.commands.SaveProductCommand;
import org.example.erp.modules.inventory.application.enums.UnitOfMeasure;
import org.example.erp.modules.partners.application.actions.CreatePartner;
import org.example.erp.modules.partners.application.commands.SavePartnerCommand;
import org.example.erp.modules.partners.application.enums.PartnerKind;
import org.example.erp.modules.purchasing.application.actions.ConfirmPurchaseOrder;
import org.example.erp.modules.purchasing.application.actions.CreatePurchaseOrder;
import org.example.erp.modules.purchasing.application.actions.ReceivePurchaseOrder;
import org.example.erp.modules.purchasing.application.actions.RecordSupplierPayment;
import org.example.erp.modules.purchasing.application.commands.CreatePurchaseOrderCommand;
import org.example.erp.modules.purchasing.application.commands.RecordSupplierPaymentCommand;
import org.example.erp.modules.sales.application.actions.IssueInvoice;
import org.example.erp.modules.sales.application.actions.RecordPayment;
import org.example.erp.modules.sales.application.commands.IssueInvoiceCommand;
import org.example.erp.modules.sales.application.commands.RecordPaymentCommand;
import org.example.erp.platform.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * One month of trading in March 2031, on its own database: owner capital, a transfer to the bank, a purchase receipt,
 * a sale, a customer receipt, two supplier payments, rent and salaries posted to an account added at runtime.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:statements;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@WithMockUser(username = "accountant", roles = "ACCOUNTANT")
class FinancialStatementsIntegrationTest {
    @Autowired CreatePartner createPartner;
    @Autowired CreateProduct createProduct;
    @Autowired CreateAccount createAccount;
    @Autowired PostJournalEntry postEntry;
    @Autowired CreatePurchaseOrder createOrder;
    @Autowired ConfirmPurchaseOrder confirmOrder;
    @Autowired ReceivePurchaseOrder receiveOrder;
    @Autowired RecordSupplierPayment paySupplier;
    @Autowired IssueInvoice issueInvoice;
    @Autowired RecordPayment receivePayment;
    @Autowired GetProfitAndLoss profitAndLoss;
    @Autowired GetBalanceSheet balanceSheet;
    @Autowired GetCashFlow cashFlow;
    @Autowired MockMvc mvc;

    private static final String CASH = LedgerRole.CASH.code();
    private static final String BANK = LedgerRole.BANK.code();

    private static BigDecimal n(String v) {
        return new BigDecimal(v);
    }

    private static LocalDate march(int day) {
        return LocalDate.of(2031, 3, day);
    }

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, n(expected).compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }

    private static Map<String, BigDecimal> amounts(List<StatementLineDTO> lines) {
        return lines.stream().collect(Collectors.toMap(StatementLineDTO::code, StatementLineDTO::amount));
    }

    private void manual(LocalDate date, String debitAccount, String creditAccount, String amount) {
        postEntry.handle(new PostEntryCommand(date, null, "Manual", List.of(
                Line.debit(debitAccount, n(amount), null, null), Line.credit(creditAccount, n(amount), null, null))), EntryKind.MANUAL);
    }

    @Test
    void statementsFollowTheLedgerAndTheBalanceSheetBalances() throws Exception {
        createAccount.handle(new CreateAccountCommand("5300", "Salaries", AccountKind.EXPENSE));
        var supplier = createPartner.handle(new SavePartnerCommand("Grain Co", PartnerKind.SUPPLIER, null, null, null, null, null, true));
        var customer = createPartner.handle(new SavePartnerCommand("Silk Road", PartnerKind.CUSTOMER, null, null, null, null, null, true));
        var flour = createProduct.handle(new SaveProductCommand("FS-1", "Flour", UnitOfMeasure.KG, n("3000"), true, null));

        manual(march(1), CASH, LedgerRole.EQUITY.code(), "1000000");
        var order = createOrder.handle(new CreatePurchaseOrderCommand(supplier.id(), march(2),
                List.of(new CreatePurchaseOrderCommand.Item(flour.id(), n("100"), n("2000")))));
        confirmOrder.handle(order.id());
        receiveOrder.handle(order.id(), march(2));
        manual(march(3), BANK, CASH, "200000");

        BalanceSheetDTO afterPurchase = balanceSheet.handle(march(4));
        assertTrue(afterPurchase.balanced());
        assertAmount("1200000", afterPurchase.totalAssets());
        assertAmount("200000", afterPurchase.totalLiabilities());
        assertAmount("0", afterPurchase.currentEarnings());
        assertAmount("1000000", afterPurchase.totalEquity());

        var invoice = issueInvoice.handle(new IssueInvoiceCommand(customer.id(), march(5),
                List.of(new IssueInvoiceCommand.Item(flour.id(), n("60"), n("3000")))));
        assertTrue(balanceSheet.handle(march(5)).balanced(), "a sale on credit keeps assets = liabilities + equity");
        receivePayment.handle(new RecordPaymentCommand(customer.id(), invoice.id(), n("180000"), org.example.erp.modules.sales.application.enums.PaymentChannel.CASH, march(6), null));
        paySupplier.handle(new RecordSupplierPaymentCommand(supplier.id(), order.id(), n("150000"),
                org.example.erp.modules.purchasing.application.enums.PaymentChannel.BANK, march(7), null));
        paySupplier.handle(new RecordSupplierPaymentCommand(supplier.id(), order.id(), n("50000"),
                org.example.erp.modules.purchasing.application.enums.PaymentChannel.CASH, march(8), null));
        manual(march(10), LedgerRole.OPERATING_EXPENSES.code(), CASH, "30000");
        manual(march(12), "5300", BANK, "10000");

        ProfitAndLossDTO pnl = profitAndLoss.handle(march(1), march(31));
        assertEquals(Map.of("4000", n("180000.00")), amounts(pnl.revenue()));
        assertEquals(Map.of("5000", n("120000.00"), "5100", n("30000.00"), "5300", n("10000.00")), amounts(pnl.expenses()),
                "cost of goods sold and the runtime-added salaries account are picked up by kind");
        assertAmount("180000", pnl.totalRevenue());
        assertAmount("160000", pnl.totalExpenses());
        assertAmount("20000", pnl.netProfit());
        ProfitAndLossDTO firstWeek = profitAndLoss.handle(march(1), march(7));
        assertAmount("60000", firstWeek.netProfit());
        assertEquals(List.of(), profitAndLoss.handle(LocalDate.of(2031, 4, 1), LocalDate.of(2031, 4, 30)).revenue());

        BalanceSheetDTO endOfMonth = balanceSheet.handle(march(31));
        assertEquals(Map.of(CASH, n("900000.00"), BANK, n("40000.00"), LedgerRole.INVENTORY.code(), n("80000.00")), amounts(endOfMonth.assets()),
                "settled receivable is left out");
        assertAmount("1020000", endOfMonth.totalAssets());
        assertEquals(List.of(), endOfMonth.liabilities(), "supplier is fully paid");
        assertAmount("20000", endOfMonth.currentEarnings());
        assertAmount("1020000", endOfMonth.totalEquity());
        assertAmount("1020000", endOfMonth.totalLiabilitiesAndEquity());
        assertTrue(endOfMonth.balanced());

        CashFlowDTO flow = cashFlow.handle(march(3), march(10));
        assertAmount("1000000", flow.opening());
        assertAmount("380000", flow.inflow());
        assertAmount("430000", flow.outflow());
        assertAmount("950000", flow.closing());
        CashFlowDTO.AccountFlow cash = flow.accounts().get(0);
        CashFlowDTO.AccountFlow bank = flow.accounts().get(1);
        assertEquals(List.of(CASH, BANK), List.of(cash.code(), bank.code()));
        assertAmount("1000000", cash.opening());
        assertAmount("180000", cash.inflow());
        assertAmount("280000", cash.outflow());
        assertAmount("900000", cash.closing());
        assertAmount("0", bank.opening());
        assertAmount("50000", bank.closing());
        assertEquals(Map.of(EntryKind.MANUAL, n("200000.00"), EntryKind.PAYMENT, n("180000.00")),
                flow.inflowsByOrigin().stream().collect(Collectors.toMap(CashFlowDTO.OriginTotal::origin, CashFlowDTO.OriginTotal::amount)));
        assertEquals(Map.of(EntryKind.MANUAL, 2, EntryKind.SUPPLIER_PAYMENT, 2),
                flow.outflowsByOrigin().stream().collect(Collectors.toMap(CashFlowDTO.OriginTotal::origin, CashFlowDTO.OriginTotal::entries)));
        assertAmount("230000", flow.outflowsByOrigin().stream().filter(o -> o.origin() == EntryKind.MANUAL).findFirst().orElseThrow().amount());
        assertEquals(List.of(march(3), march(6), march(7), march(8), march(10)), flow.days().stream().map(CashFlowDTO.Day::date).toList());
        assertAmount("950000", flow.days().getLast().closing());
        assertEquals(6, flow.movements().size(), "the bank transfer moves both accounts");
        assertAmount("950000", flow.movements().getLast().balance());

        assertThrows(BusinessRuleException.class, () -> cashFlow.handle(march(10), march(3)));

        // last, because a MockMvc request clears the test's security context when it completes
        mvc.perform(get("/api/accounting/reports/balance-sheet").param("asOf", "2031-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanced").value(true))
                .andExpect(jsonPath("$.currentEarnings").value(20000.0))
                .andExpect(jsonPath("$.totalAssets").value(1020000.0));
    }
}
