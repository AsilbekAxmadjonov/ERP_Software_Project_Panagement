package org.example.erp.modules.accounting.domain;

import org.example.erp.platform.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JournalEntryTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 12);

    private static JournalLine debit(String code, String amount) {
        return new JournalLine(code, new BigDecimal(amount), BigDecimal.ZERO, null, null);
    }

    private static JournalLine credit(String code, String amount) {
        return new JournalLine(code, BigDecimal.ZERO, new BigDecimal(amount), null, null);
    }

    @Test
    void rejectsAnUnbalancedEntry() {
        var ex = assertThrows(BusinessRuleException.class, () -> JournalEntry.post(DAY, "R1", "x", EntryOrigin.MANUAL, "u",
                List.of(debit("1000", "100"), credit("4000", "99.99"))));
        assertTrue(ex.getMessage().contains("not balanced"));
    }

    @Test
    void rejectsALineWithBothSides() {
        assertThrows(BusinessRuleException.class, () -> new JournalLine("1000", BigDecimal.TEN, BigDecimal.TEN, null, null));
    }

    @Test
    void reversalSwapsEverySideAndKeepsTheEntryBalanced() {
        JournalEntry original = JournalEntry.restore(7L, DAY, "R1", "x", EntryOrigin.MANUAL, false, null, "u",
                List.of(debit("1000", "250"), credit("3000", "250")));

        JournalEntry reversal = original.reversal(DAY, "u");

        assertEquals(EntryOrigin.REVERSAL, reversal.getOrigin());
        assertEquals(7L, reversal.getReversalOf());
        assertEquals(0, new BigDecimal("250").compareTo(reversal.getLines().get(0).credit()));
        assertEquals(0, new BigDecimal("250").compareTo(reversal.getLines().get(1).debit()));
    }

    @Test
    void onlyManualEntriesCanBeReversedByHand() {
        JournalEntry sale = JournalEntry.restore(8L, DAY, "INV-1", "x", EntryOrigin.SALE, false, null, "u",
                List.of(debit("1200", "10"), credit("4000", "10")));
        assertThrows(BusinessRuleException.class, () -> sale.reversal(DAY, "u"));
    }

    @Test
    void anEntryCannotBeReversedTwice() {
        JournalEntry entry = JournalEntry.restore(9L, DAY, "R", "x", EntryOrigin.MANUAL, true, null, "u",
                List.of(debit("1000", "10"), credit("3000", "10")));
        assertThrows(BusinessRuleException.class, () -> entry.reversal(DAY, "u"));
    }
}
