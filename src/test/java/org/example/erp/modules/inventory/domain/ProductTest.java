package org.example.erp.modules.inventory.domain;

import org.example.erp.platform.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {
    private static Product flour() {
        return Product.create("P-1", "Flour", Unit.KG, new BigDecimal("2200"), null);
    }

    private static BigDecimal n(String v) {
        return new BigDecimal(v);
    }

    @Test
    void receivingStockRecalculatesTheWeightedAverageCost() {
        Product p = flour();
        p.receive(n("100"), n("2000"));
        p.receive(n("100"), n("2200"));

        assertEquals(0, n("200").compareTo(p.getQuantity()));
        assertEquals(0, n("2100").compareTo(p.getAverageCost()));
    }

    @Test
    void issuingStockReturnsTheAverageCostAndReducesQuantity() {
        Product p = flour();
        p.receive(n("100"), n("2000"));

        BigDecimal unitCost = p.issue(n("60"));

        assertEquals(0, n("2000").compareTo(unitCost));
        assertEquals(0, n("40").compareTo(p.getQuantity()));
    }

    @Test
    void cannotIssueMoreThanIsInStock() {
        Product p = flour();
        p.receive(n("10"), n("2000"));
        assertThrows(BusinessRuleException.class, () -> p.issue(n("10.001")));
    }
}
