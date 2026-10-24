package org.example.erp.modules.purchasing.domain;

import org.example.erp.platform.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseOrderTest {
    private static final LocalDate ORDERED = LocalDate.of(2026, 10, 20);

    private static BigDecimal n(String v) {
        return new BigDecimal(v);
    }

    private static PurchaseOrder draft() {
        return PurchaseOrder.create("PO-00001", 7, "Grain Co", ORDERED,
                List.of(new PurchaseOrderLine(1, "Flour", "KG", n("100"), n("2000"))), "manager");
    }

    @Test
    void anOrderMovesFromDraftThroughConfirmedToReceived() {
        PurchaseOrder order = draft();
        assertEquals(PurchaseOrderStatus.DRAFT, order.getStatus());
        assertEquals(0, n("200000").compareTo(order.getTotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(order.billed()), "nothing is billed before receipt");

        order.confirm();
        order.receive(ORDERED.plusDays(1));

        assertEquals(PurchaseOrderStatus.RECEIVED, order.getStatus());
        assertEquals(ORDERED.plusDays(1), order.getReceivedDate());
        assertEquals(0, n("200000").compareTo(order.outstanding()));
    }

    @Test
    void aDraftCannotBeReceivedAndAReceivedOrderCannotBeConfirmedOrCancelled() {
        PurchaseOrder order = draft();
        assertThrows(BusinessRuleException.class, () -> order.receive(ORDERED));

        order.confirm();
        assertThrows(BusinessRuleException.class, order::confirm);
        order.receive(ORDERED);

        assertThrows(BusinessRuleException.class, order::cancel);
        assertThrows(BusinessRuleException.class, () -> order.receive(ORDERED));
    }

    @Test
    void draftAndConfirmedOrdersCanBeCancelledButNotReceivedAfterwards() {
        PurchaseOrder draft = draft();
        draft.cancel();
        assertEquals(PurchaseOrderStatus.CANCELLED, draft.getStatus());
        assertThrows(BusinessRuleException.class, draft::confirm);

        PurchaseOrder confirmed = draft();
        confirmed.confirm();
        confirmed.cancel();
        assertThrows(BusinessRuleException.class, () -> confirmed.receive(ORDERED));
    }

    @Test
    void goodsCannotBeReceivedBeforeTheOrderDate() {
        PurchaseOrder order = draft();
        order.confirm();
        assertThrows(BusinessRuleException.class, () -> order.receive(ORDERED.minusDays(1)));
    }

    @Test
    void paymentsApplyOnlyToReceivedOrdersAndNeverExceedTheOutstandingAmount() {
        PurchaseOrder order = draft();
        order.confirm();
        assertThrows(BusinessRuleException.class, () -> order.applyPayment(n("1")));

        order.receive(ORDERED);
        order.applyPayment(n("150000"));
        assertEquals(0, n("50000").compareTo(order.outstanding()));
        assertThrows(BusinessRuleException.class, () -> order.applyPayment(n("50000.01")));
        order.applyPayment(n("50000"));
        assertEquals(0, BigDecimal.ZERO.compareTo(order.outstanding()));
    }

    @Test
    void anOrderNeedsItemsWithAPositiveTotal() {
        assertThrows(BusinessRuleException.class, () -> PurchaseOrder.create("PO-1", 7, "Grain Co", ORDERED, List.of(), "m"));
        assertThrows(BusinessRuleException.class, () -> PurchaseOrder.create("PO-1", 7, "Grain Co", ORDERED,
                List.of(new PurchaseOrderLine(1, "Flour", "KG", n("5"), n("0"))), "m"));
        assertThrows(BusinessRuleException.class, () -> new PurchaseOrderLine(1, "Flour", "KG", n("0"), n("10")));
    }
}
