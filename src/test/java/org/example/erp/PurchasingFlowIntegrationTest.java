package org.example.erp;

import org.example.erp.modules.accounting.application.actions.GetPartnerBalance;
import org.example.erp.modules.accounting.application.actions.GetTrialBalance;
import org.example.erp.modules.accounting.application.dto.TrialBalanceDTO;
import org.example.erp.modules.accounting.application.enums.LedgerRole;
import org.example.erp.modules.inventory.application.actions.CreateProduct;
import org.example.erp.modules.inventory.application.actions.FindProduct;
import org.example.erp.modules.inventory.application.actions.ListStockMovements;
import org.example.erp.modules.inventory.application.commands.SaveProductCommand;
import org.example.erp.modules.inventory.application.enums.MovementKind;
import org.example.erp.modules.inventory.application.enums.UnitOfMeasure;
import org.example.erp.modules.partners.application.actions.CreatePartner;
import org.example.erp.modules.partners.application.commands.SavePartnerCommand;
import org.example.erp.modules.partners.application.enums.PartnerKind;
import org.example.erp.modules.purchasing.application.actions.*;
import org.example.erp.modules.purchasing.application.commands.CreatePurchaseOrderCommand;
import org.example.erp.modules.purchasing.application.commands.RecordSupplierPaymentCommand;
import org.example.erp.modules.purchasing.application.dto.PurchaseOrderDTO;
import org.example.erp.modules.purchasing.application.enums.OrderStatus;
import org.example.erp.modules.purchasing.application.enums.PaymentChannel;
import org.example.erp.modules.purchasing.application.enums.PaymentProgress;
import org.example.erp.platform.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Buy 100 kg of flour at 2,000/kg, receive it, then pay the supplier in two instalments. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:purchaseflow;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@WithMockUser(username = "manager", roles = "MANAGER")
class PurchasingFlowIntegrationTest {
    @Autowired CreatePartner createPartner;
    @Autowired CreateProduct createProduct;
    @Autowired FindProduct findProduct;
    @Autowired ListStockMovements listMovements;
    @Autowired CreatePurchaseOrder createOrder;
    @Autowired ConfirmPurchaseOrder confirmOrder;
    @Autowired ReceivePurchaseOrder receiveOrder;
    @Autowired CancelPurchaseOrder cancelOrder;
    @Autowired FindPurchaseOrder findOrder;
    @Autowired RecordSupplierPayment recordPayment;
    @Autowired GetTrialBalance trialBalance;
    @Autowired GetPartnerBalance partnerBalance;
    @Autowired MockMvc mvc;

    private static BigDecimal n(String v) {
        return new BigDecimal(v);
    }

    private BigDecimal balanceOf(TrialBalanceDTO tb, String code) {
        return tb.rows().stream().filter(r -> r.code().equals(code)).findFirst()
                .map(r -> r.debit().subtract(r.credit())).orElse(BigDecimal.ZERO);
    }

    @Test
    void receiptIncreasesStockAndPostsThePayableWhichSupplierPaymentsClear() throws Exception {
        var supplier = createPartner.handle(new SavePartnerCommand("Grain Co", PartnerKind.SUPPLIER, null, "+998", null, null, null, true));
        var flour = createProduct.handle(new SaveProductCommand("PF-1", "Flour", UnitOfMeasure.KG, n("2200"), true, null));
        TrialBalanceDTO before = trialBalance.handle();

        PurchaseOrderDTO order = createOrder.handle(new CreatePurchaseOrderCommand(supplier.id(), null,
                List.of(new CreatePurchaseOrderCommand.Item(flour.id(), n("100"), n("2000")))));
        assertTrue(order.number().matches("PO-\\d{5}"));
        assertEquals(OrderStatus.DRAFT, order.status());
        assertEquals(0, n("200000").compareTo(order.total()));
        assertEquals(0, BigDecimal.ZERO.compareTo(findProduct.handle(flour.id()).quantity()), "a draft order moves no stock");

        confirmOrder.handle(order.id());
        PurchaseOrderDTO received = receiveOrder.handle(order.id(), null);

        assertEquals(OrderStatus.RECEIVED, received.status());
        assertEquals(0, n("200000").compareTo(received.outstanding()));
        var stock = findProduct.handle(flour.id());
        assertEquals(0, n("100").compareTo(stock.quantity()));
        assertEquals(0, n("2000").compareTo(stock.averageCost()));
        var movement = listMovements.handle(flour.id()).getFirst();
        assertEquals(MovementKind.PURCHASE, movement.type());
        assertEquals(order.number(), movement.reference());

        TrialBalanceDTO tb = trialBalance.handle();
        assertEquals(0, tb.totalDebit().compareTo(tb.totalCredit()), "ledger must stay balanced");
        assertEquals(0, n("200000").compareTo(balanceOf(tb, "1300").subtract(balanceOf(before, "1300"))));
        assertEquals(0, n("-200000").compareTo(balanceOf(tb, "2000").subtract(balanceOf(before, "2000"))));
        assertEquals(0, n("-200000").compareTo(partnerBalance.handle(supplier.id(), LedgerRole.PAYABLE)));


        recordPayment.handle(new RecordSupplierPaymentCommand(supplier.id(), order.id(), n("150000"), PaymentChannel.BANK, null, null));
        assertEquals(PaymentProgress.PARTIAL, findOrder.handle(order.id()).paymentStatus());
        assertThrows(BusinessRuleException.class, () -> recordPayment.handle(
                new RecordSupplierPaymentCommand(supplier.id(), order.id(), n("50000.01"), PaymentChannel.CASH, null, null)));
        recordPayment.handle(new RecordSupplierPaymentCommand(supplier.id(), order.id(), n("50000"), PaymentChannel.CASH, null, null));

        var paid = findOrder.handle(order.id());
        assertEquals(PaymentProgress.PAID, paid.paymentStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(paid.outstanding()));
        assertEquals(0, BigDecimal.ZERO.compareTo(partnerBalance.handle(supplier.id(), LedgerRole.PAYABLE)));
        TrialBalanceDTO after = trialBalance.handle();
        assertEquals(0, after.totalDebit().compareTo(after.totalCredit()));
        assertEquals(0, n("-150000").compareTo(balanceOf(after, "1100").subtract(balanceOf(tb, "1100"))));
        assertEquals(0, n("-50000").compareTo(balanceOf(after, "1000").subtract(balanceOf(tb, "1000"))));

        // last, because a MockMvc request clears the test's security context when it completes
        mvc.perform(get("/api/supplier-balances")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.partnerId == %d)].balance".formatted(supplier.id())).value(0.0));
    }

    @Test
    void purchaseReportListsReceivedItemsOfTheRequestedSupplier() throws Exception {
        var supplier = createPartner.handle(new SavePartnerCommand("Sugar Co", PartnerKind.SUPPLIER, null, null, null, null, null, true));
        var other = createPartner.handle(new SavePartnerCommand("Salt Co", PartnerKind.SUPPLIER, null, null, null, null, null, true));
        var sugar = createProduct.handle(new SaveProductCommand("PR-SUG", "Sugar", UnitOfMeasure.KG, n("9000"), true, null));
        var items = List.of(new CreatePurchaseOrderCommand.Item(sugar.id(), n("5"), n("8000")));
        PurchaseOrderDTO received = createOrder.handle(new CreatePurchaseOrderCommand(supplier.id(), null, items));
        confirmOrder.handle(received.id());
        receiveOrder.handle(received.id(), null);
        createOrder.handle(new CreatePurchaseOrderCommand(supplier.id(), null, items));
        PurchaseOrderDTO otherOrder = createOrder.handle(new CreatePurchaseOrderCommand(other.id(), null, items));
        confirmOrder.handle(otherOrder.id());
        receiveOrder.handle(otherOrder.id(), null);

        mvc.perform(get("/api/purchase/report").param("supplierId", String.valueOf(supplier.id())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].order").value(received.number()))
                .andExpect(jsonPath("$[0].supplier").value("Sugar Co"))
                .andExpect(jsonPath("$[0].product").value("Sugar"))
                .andExpect(jsonPath("$[0].total").value(40000.0));
    }

    @Test
    void customersCannotBeOrderedFromAndCancelledOrdersCannotBeReceived() {
        var customer = createPartner.handle(new SavePartnerCommand("Retail", PartnerKind.CUSTOMER, null, null, null, null, null, true));
        var supplier = createPartner.handle(new SavePartnerCommand("Oil Co", PartnerKind.BOTH, null, null, null, null, null, true));
        var oil = createProduct.handle(new SaveProductCommand("PO-OIL", "Oil", UnitOfMeasure.PCS, n("15000"), true, null));
        var items = List.of(new CreatePurchaseOrderCommand.Item(oil.id(), n("10"), n("12000")));

        assertThrows(BusinessRuleException.class, () -> createOrder.handle(new CreatePurchaseOrderCommand(customer.id(), null, items)));

        PurchaseOrderDTO order = createOrder.handle(new CreatePurchaseOrderCommand(supplier.id(), null, items));
        confirmOrder.handle(order.id());
        assertEquals(OrderStatus.CANCELLED, cancelOrder.handle(order.id()).status());
        assertThrows(BusinessRuleException.class, () -> receiveOrder.handle(order.id(), null));
        assertEquals(0, BigDecimal.ZERO.compareTo(findProduct.handle(oil.id()).quantity()));
    }
}
