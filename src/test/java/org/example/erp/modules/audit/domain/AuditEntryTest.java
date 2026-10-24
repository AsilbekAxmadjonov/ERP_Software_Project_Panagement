package org.example.erp.modules.audit.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuditEntryTest {
    private static final Instant AT = Instant.parse("2026-10-01T09:30:00Z");

    @Test
    void describesAnyEventFromItsNameAndTheMostSpecificReference() {
        AuditEntry entry = AuditEntry.record(AT, "accountant", "sales.invoice-issued",
                Map.of("invoiceNumber", "INV-00007", "customerName", "Silk Road", "total", "132000.00"));

        assertEquals("INV-00007", entry.getSubject());
        assertEquals("Invoice issued INV-00007", entry.getSummary());
        assertEquals("accountant", entry.getActor());
    }

    @Test
    void eventsFromModulesItHasNeverSeenStillGetASummary() {
        AuditEntry entry = AuditEntry.record(AT, "ceo", "expenses.expense_recorded", Map.of("voucherNumber", "EXP-3"));

        assertEquals("EXP-3", entry.getSubject());
        assertEquals("Expense recorded EXP-3", entry.getSummary());
    }

    @Test
    void dropsSecretLookingKeysAtEveryLevel() {
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("apiToken", "abc");
        nested.put("note", "kept");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("username", "dilnoza");
        payload.put("password", "hunter2hunter2");
        payload.put("passwordHash", "$2a$10$x");
        payload.put("clientSecret", "s");
        payload.put("passwordChanged", true);
        payload.put("details", nested);
        payload.put("items", List.of(Map.of("refreshToken", "r", "qty", 2)));

        Map<String, Object> stored = AuditEntry.record(AT, "ceo", "identity.user-created", payload).getPayload();

        assertEquals("dilnoza", stored.get("username"));
        assertFalse(stored.containsKey("password"));
        assertFalse(stored.containsKey("passwordHash"));
        assertFalse(stored.containsKey("clientSecret"));
        assertEquals(true, stored.get("passwordChanged"), "a flag reveals no secret");
        assertEquals(Map.of("note", "kept"), stored.get("details"));
        assertEquals(List.of(Map.of("qty", 2)), stored.get("items"));
    }

    @Test
    void anEntryNeedsAnActionAndATime() {
        assertThrows(IllegalArgumentException.class, () -> AuditEntry.record(AT, "ceo", " ", Map.of()));
        assertThrows(IllegalArgumentException.class, () -> AuditEntry.record(null, "ceo", "sales.x", Map.of()));
    }
}
