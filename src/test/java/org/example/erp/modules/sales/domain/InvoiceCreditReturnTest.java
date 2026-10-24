package org.example.erp.modules.sales.domain;

import org.example.erp.platform.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceCreditReturnTest {
    private static final LocalDate SOLD = LocalDate.of(2026, 10, 20);

    private static BigDecimal n(String v) {
        return new BigDecimal(v);
    }

    /** 3 kg of flour at 10.00 (cost 7.0000), and 1 pc of oil at 15.00 (cost 12.0000). */
    private static Invoice invoice() {
        return Invoice.restore(1L, "INV-00001", 5, "Market", SOLD, List.of(
                new InvoiceLine(1, "Flour", "KG", n("3.000"), n("10.00"), n("7.0000")),
                new InvoiceLine(2, "Oil", "PCS", n("1.000"), n("15.00"), n("12.0000"))),
                n("0.00"), n("0.00"), "accountant");
    }

    @Test
    void returningGoodsCreditsTheSoldPriceAndCostAndReducesWhatIsOwed() {
        Invoice invoice = invoice();

        CreditNote note = invoice.creditReturn("CN-00001", SOLD, "Damaged", List.of(new ReturnItem(1, n("2"))), "accountant");

        assertEquals(0, n("20.00").compareTo(note.getTotal()));
        assertEquals(0, n("14.00").compareTo(note.getCost()));
        assertEquals(0, n("20.00").compareTo(invoice.getCredited()));
        assertEquals(0, n("25.00").compareTo(invoice.outstanding()));
        assertEquals(0, n("1").compareTo(invoice.getLines().getFirst().returnable()));
        assertEquals("INV-00001", note.getInvoiceNumber());
    }

    @Test
    void cannotReturnMoreThanSoldMinusAlreadyReturned() {
        Invoice invoice = invoice();
        invoice.creditReturn("CN-00001", SOLD, null, List.of(new ReturnItem(1, n("2"))), "a");

        assertThrows(BusinessRuleException.class,
                () -> invoice.creditReturn("CN-00002", SOLD, null, List.of(new ReturnItem(1, n("1.001"))), "a"));
        assertThrows(BusinessRuleException.class,
                () -> invoice.creditReturn("CN-00002", SOLD, null, List.of(new ReturnItem(1, n("0.5")), new ReturnItem(1, n("0.6"))), "a"));
        assertEquals(0, n("20.00").compareTo(invoice.getCredited()), "a rejected return changes nothing");
        assertEquals(0, n("1").compareTo(invoice.getLines().getFirst().returnable()));
    }

    @Test
    void productsNotOnTheInvoiceZeroQuantitiesAndEarlyDatesAreRejected() {
        Invoice invoice = invoice();
        assertThrows(BusinessRuleException.class, () -> invoice.creditReturn("CN-1", SOLD, null, List.of(new ReturnItem(9, n("1"))), "a"));
        assertThrows(BusinessRuleException.class, () -> invoice.creditReturn("CN-1", SOLD, null, List.of(new ReturnItem(1, n("0"))), "a"));
        assertThrows(BusinessRuleException.class, () -> invoice.creditReturn("CN-1", SOLD, null, List.of(), "a"));
        assertThrows(BusinessRuleException.class,
                () -> invoice.creditReturn("CN-1", SOLD.minusDays(1), null, List.of(new ReturnItem(1, n("1"))), "a"));
    }

    @Test
    void returningEverythingInStepsCreditsExactlyTheInvoiceTotal() {
        Invoice invoice = Invoice.restore(1L, "INV-00002", 5, "Market", SOLD,
                List.of(new InvoiceLine(1, "Flour", "KG", n("1.000"), n("0.05"), n("0.0300"))), n("0.00"), n("0.00"), "a");

        invoice.creditReturn("CN-1", SOLD, null, List.of(new ReturnItem(1, n("0.5"))), "a");
        invoice.creditReturn("CN-2", SOLD, null, List.of(new ReturnItem(1, n("0.5"))), "a");

        assertEquals(0, invoice.getTotal().compareTo(invoice.getCredited()));
        assertTrue(invoice.isFullyReturned());
        assertEquals(0, BigDecimal.ZERO.compareTo(invoice.outstanding()));
    }

    @Test
    void aRefundBeyondWhatIsStillOwedLeavesNothingOutstanding() {
        Invoice invoice = invoice();
        invoice.applyPayment(n("45.00"));

        invoice.creditReturn("CN-1", SOLD, null, List.of(new ReturnItem(2, n("1"))), "a");

        assertEquals(0, BigDecimal.ZERO.compareTo(invoice.outstanding()));
        assertThrows(BusinessRuleException.class, () -> invoice.applyPayment(n("0.01")));
    }
}
