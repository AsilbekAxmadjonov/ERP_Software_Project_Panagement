package org.example.erp;

import org.example.erp.modules.accounting.application.actions.ClosePeriod;
import org.example.erp.modules.accounting.application.actions.GetTrialBalance;
import org.example.erp.modules.accounting.application.actions.PostJournalEntry;
import org.example.erp.modules.accounting.application.actions.ReopenPeriod;
import org.example.erp.modules.accounting.application.commands.PostEntryCommand;
import org.example.erp.modules.accounting.application.dto.TrialBalanceDTO;
import org.example.erp.modules.accounting.application.enums.EntryKind;
import org.example.erp.modules.inventory.application.actions.AdjustStock;
import org.example.erp.modules.inventory.application.actions.CreateProduct;
import org.example.erp.modules.inventory.application.actions.FindProduct;
import org.example.erp.modules.inventory.application.actions.ListLowStockProducts;
import org.example.erp.modules.inventory.application.commands.AdjustStockCommand;
import org.example.erp.modules.inventory.application.commands.SaveProductCommand;
import org.example.erp.modules.inventory.application.enums.UnitOfMeasure;
import org.example.erp.modules.partners.application.actions.CreatePartner;
import org.example.erp.modules.partners.application.commands.SavePartnerCommand;
import org.example.erp.modules.partners.application.enums.PartnerKind;
import org.example.erp.modules.sales.application.actions.FindInvoice;
import org.example.erp.modules.sales.application.actions.IssueCreditNote;
import org.example.erp.modules.sales.application.actions.IssueInvoice;
import org.example.erp.modules.sales.application.actions.ListCreditNotes;
import org.example.erp.modules.sales.application.actions.ListInvoices;
import org.example.erp.modules.sales.application.commands.IssueCreditNoteCommand;
import org.example.erp.modules.sales.application.commands.IssueInvoiceCommand;
import org.example.erp.modules.sales.application.dto.CreditNoteDTO;
import org.example.erp.modules.sales.application.dto.InvoiceDTO;
import org.example.erp.modules.sales.application.enums.PaymentProgress;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:lockandreturns;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@WithMockUser(username = "accountant", roles = "ACCOUNTANT")
class PeriodLockAndReturnsIntegrationTest {
    @Autowired CreatePartner createPartner;
    @Autowired CreateProduct createProduct;
    @Autowired AdjustStock adjustStock;
    @Autowired FindProduct findProduct;
    @Autowired ListLowStockProducts lowStock;
    @Autowired IssueInvoice issueInvoice;
    @Autowired FindInvoice findInvoice;
    @Autowired ListInvoices listInvoices;
    @Autowired IssueCreditNote issueCreditNote;
    @Autowired ListCreditNotes listCreditNotes;
    @Autowired PostJournalEntry postEntry;
    @Autowired ClosePeriod closePeriod;
    @Autowired ReopenPeriod reopenPeriod;
    @Autowired GetTrialBalance trialBalance;
    @Autowired MockMvc mvc;

    private static BigDecimal n(String v) {
        return new BigDecimal(v);
    }

    private BigDecimal balanceOf(TrialBalanceDTO tb, String code) {
        return tb.rows().stream().filter(r -> r.code().equals(code)).findFirst()
                .map(r -> r.debit().subtract(r.credit())).orElse(BigDecimal.ZERO);
    }

    @Test
    void aClosedPeriodRejectsManualEntriesAndSalesDatedInItWithoutLeavingATrace() throws Exception {
        LocalDate today = LocalDate.now();
        var customer = createPartner.handle(new SavePartnerCommand("Locked Market", PartnerKind.CUSTOMER, null, null, null, null, null, true));
        var rice = createProduct.handle(new SaveProductCommand("L-RICE", "Rice", UnitOfMeasure.KG, n("1000"), true, null));
        adjustStock.handle(new AdjustStockCommand(rice.id(), n("10"), n("800"), "Opening stock"));
        int invoicesBefore = listInvoices.handle().size();

        closePeriod.handle(today.minusDays(10));
        try {
            assertThrows(BusinessRuleException.class, () -> postEntry.handle(new PostEntryCommand(today.minusDays(10), "M-1", "Late",
                    List.of(PostEntryCommand.Line.debit("1000", n("5"), null, null),
                            PostEntryCommand.Line.credit("3000", n("5"), null, null))), EntryKind.MANUAL));
            assertThrows(BusinessRuleException.class, () -> issueInvoice.handle(new IssueInvoiceCommand(customer.id(),
                    today.minusDays(20), List.of(new IssueInvoiceCommand.Item(rice.id(), n("2"), null)))));

            assertEquals(invoicesBefore, listInvoices.handle().size(), "the invoice rolls back with its ledger entry");
            assertEquals(0, n("10").compareTo(findProduct.handle(rice.id()).quantity()), "stock is not issued either");

            issueInvoice.handle(new IssueInvoiceCommand(customer.id(), today.minusDays(9),
                    List.of(new IssueInvoiceCommand.Item(rice.id(), n("2"), null))));
        } finally {
            reopenPeriod.handle(null);
        }
    }

    @Test
    void onlyTheCeoMayReopenAPeriod() throws Exception {
        mvc.perform(post("/api/accounting/periods/reopen").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ceo", roles = "CEO")
    void theCeoMayReopenAPeriodOverHttp() throws Exception {
        closePeriod.handle(LocalDate.now().minusDays(30));
        mvc.perform(post("/api/accounting/periods/reopen").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.lockDate").doesNotExist());
    }

    @Test
    void aCreditNoteReturnsGoodsAtCostAndReversesRevenueAndReceivable() {
        var customer = createPartner.handle(new SavePartnerCommand("Return Market", PartnerKind.CUSTOMER, null, null, null, null, null, true));
        var sugar = createProduct.handle(new SaveProductCommand("R-SUG", "Sugar", UnitOfMeasure.KG, n("1800"), true, n("50")));
        adjustStock.handle(new AdjustStockCommand(sugar.id(), n("100"), n("1500"), "Opening stock"));
        InvoiceDTO invoice = issueInvoice.handle(new IssueInvoiceCommand(customer.id(), null,
                List.of(new IssueInvoiceCommand.Item(sugar.id(), n("60"), null))));
        assertTrue(findProduct.handle(sugar.id()).lowStock(), "40 kg left is below the reorder level of 50");
        assertTrue(lowStock.handle().stream().anyMatch(p -> p.id() == sugar.id()));
        TrialBalanceDTO before = trialBalance.handle();

        CreditNoteDTO note = issueCreditNote.handle(new IssueCreditNoteCommand(invoice.id(), null, "Wet bags",
                List.of(new IssueCreditNoteCommand.Item(sugar.id(), n("20")))));

        assertTrue(note.number().matches("CN-\\d{5}"));
        assertEquals(0, n("36000").compareTo(note.total()));
        assertEquals(0, n("30000").compareTo(note.cost()));
        var stock = findProduct.handle(sugar.id());
        assertEquals(0, n("60").compareTo(stock.quantity()));
        assertEquals(0, n("1500").compareTo(stock.averageCost()));
        assertFalse(stock.lowStock());

        InvoiceDTO reloaded = findInvoice.handle(invoice.id());
        assertEquals(0, n("36000").compareTo(reloaded.credited()));
        assertEquals(0, n("72000").compareTo(reloaded.outstanding()));
        assertEquals(PaymentProgress.PARTIAL, reloaded.status());
        assertEquals(0, n("40").compareTo(reloaded.lines().getFirst().returnable()));

        TrialBalanceDTO after = trialBalance.handle();
        assertEquals(0, after.totalDebit().compareTo(after.totalCredit()), "ledger must stay balanced");
        assertEquals(0, n("36000").compareTo(balanceOf(after, "4000").subtract(balanceOf(before, "4000"))));
        assertEquals(0, n("-36000").compareTo(balanceOf(after, "1200").subtract(balanceOf(before, "1200"))));
        assertEquals(0, n("30000").compareTo(balanceOf(after, "1300").subtract(balanceOf(before, "1300"))));
        assertEquals(0, n("-30000").compareTo(balanceOf(after, "5000").subtract(balanceOf(before, "5000"))));

        assertThrows(BusinessRuleException.class, () -> issueCreditNote.handle(new IssueCreditNoteCommand(invoice.id(), null, null,
                List.of(new IssueCreditNoteCommand.Item(sugar.id(), n("40.001"))))));
        issueCreditNote.handle(new IssueCreditNoteCommand(invoice.id(), null, null,
                List.of(new IssueCreditNoteCommand.Item(sugar.id(), n("40")))));
        assertEquals(PaymentProgress.CREDITED, findInvoice.handle(invoice.id()).status());
        assertEquals(2, listCreditNotes.handle(invoice.id()).size());
    }
}
