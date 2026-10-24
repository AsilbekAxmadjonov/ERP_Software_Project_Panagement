package org.example.erp.modules.expenses.domain;

import org.example.erp.platform.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CashEntryTest {
    private static final LocalDate PAID = LocalDate.of(2026, 10, 5);
    private static final Category RENT = Category.restore(1L, "Rent", CategoryKind.EXPENSE, "6100", true);
    private static final Category INTEREST = Category.restore(2L, "Interest", CategoryKind.INCOME, "4100", true);

    private static CashEntry rent(String amount) {
        return CashEntry.record(CategoryKind.EXPENSE, RENT, PAID, new BigDecimal(amount), PaymentMethod.CASH, "  October rent ", "accountant");
    }

    @Test
    void anExpenseCopiesItsCategoryAccountAndStartsPosted() {
        CashEntry entry = rent("3000000.00");

        assertEquals(EntryStatus.POSTED, entry.getStatus());
        assertEquals("6100", entry.getAccountCode());
        assertEquals("Rent", entry.getCategoryName());
        assertEquals("October rent", entry.getDescription());
    }

    @Test
    void theAmountMustBePositive() {
        assertThrows(BusinessRuleException.class, () -> rent("0"));
        assertThrows(BusinessRuleException.class, () -> rent("-10"));
    }

    @Test
    void theCategoryMustMatchTheEntryTypeAndBeActive() {
        assertThrows(BusinessRuleException.class, () -> CashEntry.record(CategoryKind.EXPENSE, INTEREST, PAID, BigDecimal.TEN,
                PaymentMethod.BANK, null, "accountant"));
        Category archived = Category.restore(3L, "Old", CategoryKind.EXPENSE, "6900", false);
        assertThrows(BusinessRuleException.class, () -> CashEntry.record(CategoryKind.EXPENSE, archived, PAID, BigDecimal.TEN,
                PaymentMethod.BANK, null, "accountant"));
    }

    @Test
    void anEntryIsCancelledOnceAndNotBeforeItsOwnDate() {
        CashEntry entry = CashEntry.restore(12L, CategoryKind.EXPENSE, 1, "Rent", "6100", PAID, new BigDecimal("100.00"),
                PaymentMethod.CASH, null, EntryStatus.POSTED, null, "accountant");
        assertEquals("EXP-00012", entry.reference());
        assertThrows(BusinessRuleException.class, () -> entry.cancel(PAID.minusDays(1)));

        entry.cancel(PAID.plusDays(3));
        assertEquals(EntryStatus.CANCELLED, entry.getStatus());
        assertEquals(PAID.plusDays(3), entry.getCancelledOn());
        assertThrows(BusinessRuleException.class, () -> entry.cancel(PAID.plusDays(4)));
    }

    @Test
    void aCategoryNeedsANameAndAnAccount() {
        assertThrows(BusinessRuleException.class, () -> Category.create(" ", CategoryKind.EXPENSE, "6100"));
        assertThrows(BusinessRuleException.class, () -> Category.create("Rent", CategoryKind.EXPENSE, ""));
        assertEquals("Fuel", Category.create(" Fuel ", CategoryKind.EXPENSE, "6300").getName());
    }
}
