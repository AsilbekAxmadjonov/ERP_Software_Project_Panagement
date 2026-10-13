package org.example.erp;

import org.example.erp.modules.accounting.application.actions.GetPartnerLedger;
import org.example.erp.modules.accounting.application.actions.GetTrialBalance;
import org.example.erp.modules.accounting.application.dto.TrialBalanceDTO;
import org.example.erp.modules.inventory.application.actions.AdjustStock;
import org.example.erp.modules.inventory.application.actions.CreateProduct;
import org.example.erp.modules.inventory.application.actions.ListProducts;
import org.example.erp.modules.inventory.application.commands.AdjustStockCommand;
import org.example.erp.modules.inventory.application.commands.SaveProductCommand;
import org.example.erp.modules.inventory.application.enums.UnitOfMeasure;
import org.example.erp.modules.partners.application.actions.CreatePartner;
import org.example.erp.modules.partners.application.commands.SavePartnerCommand;
import org.example.erp.modules.partners.application.enums.PartnerKind;
import org.example.erp.modules.sales.application.actions.IssueInvoice;
import org.example.erp.modules.sales.application.actions.RecordPayment;
import org.example.erp.modules.sales.application.commands.IssueInvoiceCommand;
import org.example.erp.modules.sales.application.commands.RecordPaymentCommand;
import org.example.erp.modules.sales.application.dto.InvoiceDTO;
import org.example.erp.modules.sales.application.enums.PaymentChannel;
import org.example.erp.modules.sales.application.enums.PaymentProgress;
import org.example.erp.platform.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The requirements example: sell 60 kg that cost 2,000/kg at 2,200/kg and keep the ledger balanced. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:salesflow;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@WithMockUser(username = "accountant", roles = "ACCOUNTANT")
class SalesFlowIntegrationTest {
    @Autowired CreatePartner createPartner;
    @Autowired CreateProduct createProduct;
    @Autowired AdjustStock adjustStock;
    @Autowired ListProducts listProducts;
    @Autowired IssueInvoice issueInvoice;
    @Autowired RecordPayment recordPayment;
    @Autowired GetTrialBalance trialBalance;
    @Autowired GetPartnerLedger partnerLedger;

    private static BigDecimal n(String v) {
        return new BigDecimal(v);
    }

    private BigDecimal balanceOf(TrialBalanceDTO tb, String code) {
        return tb.rows().stream().filter(r -> r.code().equals(code)).findFirst()
                .map(r -> r.debit().subtract(r.credit())).orElse(BigDecimal.ZERO);
    }

    @Test
    void saleReducesStockPostsRevenueAndCostAndPaymentClearsTheReceivable() {
        var customer = createPartner.handle(new SavePartnerCommand("Silk Road", PartnerKind.CUSTOMER, null, null, null, null, null, true));
        var flour = createProduct.handle(new SaveProductCommand("F-1", "Flour", UnitOfMeasure.KG, n("2200"), true));
        adjustStock.handle(new AdjustStockCommand(flour.id(), n("100"), n("2000"), "Opening stock"));
        TrialBalanceDTO before = trialBalance.handle();

        InvoiceDTO invoice = issueInvoice.handle(new IssueInvoiceCommand(customer.id(), null,
                List.of(new IssueInvoiceCommand.Item(flour.id(), n("60"), null))));

        assertEquals(0, n("132000").compareTo(invoice.total()));
        assertEquals(0, n("120000").compareTo(invoice.cost()));
        var stock = listProducts.handle().stream().filter(p -> p.id() == flour.id()).findFirst().orElseThrow();
        assertEquals(0, n("40").compareTo(stock.quantity()));

        TrialBalanceDTO tb = trialBalance.handle();
        assertEquals(0, tb.totalDebit().compareTo(tb.totalCredit()), "ledger must stay balanced");
        assertEquals(0, n("-132000").compareTo(balanceOf(tb, "4000").subtract(balanceOf(before, "4000"))));
        assertEquals(0, n("120000").compareTo(balanceOf(tb, "5000").subtract(balanceOf(before, "5000"))));
        assertEquals(0, n("132000").compareTo(partnerLedger.handle(customer.id()).getLast().balance()));

        var payment = recordPayment.handle(new RecordPaymentCommand(customer.id(), invoice.id(), n("132000"), PaymentChannel.CASH, null, null));
        assertEquals(invoice.number(), payment.invoiceNumber());
        assertEquals(0, BigDecimal.ZERO.compareTo(partnerLedger.handle(customer.id()).getLast().balance()));
        TrialBalanceDTO after = trialBalance.handle();
        assertEquals(0, after.totalDebit().compareTo(after.totalCredit()));
        assertEquals(0, n("132000").compareTo(balanceOf(after, "1000").subtract(balanceOf(tb, "1000"))));
    }

    @Test
    void sellingMoreThanInStockFailsAndLeavesNoInvoiceOrLedgerTrace() {
        var customer = createPartner.handle(new SavePartnerCommand("Market", PartnerKind.CUSTOMER, null, null, null, null, null, true));
        var sugar = createProduct.handle(new SaveProductCommand("S-1", "Sugar", UnitOfMeasure.KG, n("1800"), true));
        adjustStock.handle(new AdjustStockCommand(sugar.id(), n("5"), n("1500"), "Opening stock"));
        BigDecimal debitBefore = trialBalance.handle().totalDebit();

        assertThrows(BusinessRuleException.class, () -> issueInvoice.handle(new IssueInvoiceCommand(customer.id(), null,
                List.of(new IssueInvoiceCommand.Item(sugar.id(), n("6"), null)))));

        assertEquals(0, debitBefore.compareTo(trialBalance.handle().totalDebit()));
        assertTrue(partnerLedger.handle(customer.id()).isEmpty());
    }

    @Test
    void partialPaymentLeavesTheInvoicePartiallyPaid() {
        var customer = createPartner.handle(new SavePartnerCommand("Fergana", PartnerKind.BOTH, null, null, null, null, null, true));
        var oil = createProduct.handle(new SaveProductCommand("O-1", "Oil", UnitOfMeasure.PCS, n("15000"), true));
        adjustStock.handle(new AdjustStockCommand(oil.id(), n("10"), n("12000"), "Opening stock"));
        InvoiceDTO invoice = issueInvoice.handle(new IssueInvoiceCommand(customer.id(), null,
                List.of(new IssueInvoiceCommand.Item(oil.id(), n("2"), null))));

        recordPayment.handle(new RecordPaymentCommand(customer.id(), invoice.id(), n("10000"), PaymentChannel.BANK, null, null));

        assertThrows(BusinessRuleException.class, () -> recordPayment.handle(
                new RecordPaymentCommand(customer.id(), invoice.id(), n("20001"), PaymentChannel.BANK, null, null)));
        var reloaded = issueInvoiceListing(invoice.id());
        assertEquals(PaymentProgress.PARTIAL, reloaded.status());
    }

    @Autowired org.example.erp.modules.sales.application.actions.ListInvoices listInvoices;

    private InvoiceDTO issueInvoiceListing(long id) {
        return listInvoices.handle().stream().filter(i -> i.id() == id).findFirst().orElseThrow();
    }
}
