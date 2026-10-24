package org.example.erp.modules.audit.domain;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builds a human summary for any event without knowing the publishing module: "sales.invoice-issued" with an
 * invoiceNumber becomes "Invoice issued INV-00001". Modules added later are described the same way.
 */
final class AuditDescription {
    /** Payload keys that identify the record an event is about, most specific first. */
    private static final List<String> SUBJECT_KEYS = List.of("invoiceNumber", "creditNoteNumber", "orderNumber",
            "entryNumber", "number", "reference", "code", "username", "employeeName", "productName", "partnerName",
            "accountName", "customerName", "supplierName", "name", "lockDate");

    private AuditDescription() {}

    static String subjectOf(Map<String, Object> payload) {
        for (String key : SUBJECT_KEYS) {
            Object value = payload.get(key);
            if (value != null && !value.toString().isBlank()) return value.toString();
        }
        return payload.entrySet().stream()
                .filter(e -> e.getKey().endsWith("Number") && e.getValue() != null)
                .map(e -> e.getValue().toString()).findFirst().orElse(null);
    }

    static String summarize(String action, String subject) {
        String verb = action.contains(".") ? action.substring(action.indexOf('.') + 1) : action;
        String words = verb.replace('-', ' ').replace('_', ' ').trim();
        String sentence = words.isEmpty() ? action : words.substring(0, 1).toUpperCase(Locale.ROOT) + words.substring(1);
        return subject == null ? sentence : sentence + " " + subject;
    }
}
