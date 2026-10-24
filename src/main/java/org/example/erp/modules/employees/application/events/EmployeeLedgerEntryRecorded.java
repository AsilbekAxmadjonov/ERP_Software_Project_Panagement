package org.example.erp.modules.employees.application.events;

import org.example.erp.modules.employees.application.enums.LedgerEntryKind;
import org.example.erp.modules.employees.application.enums.PaymentChannel;
import org.example.erp.platform.BaseIntegrationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/** A movement on an employee's account; channel is set only when money was paid out (payment, advance). */
public final class EmployeeLedgerEntryRecorded extends BaseIntegrationEvent {
    private final String reference;
    private final long employeeId;
    private final String employeeName;
    private final LocalDate date;
    private final LedgerEntryKind type;
    private final BigDecimal amount;
    private final PaymentChannel channel;

    public EmployeeLedgerEntryRecorded(String reference, long employeeId, String employeeName, LocalDate date, LedgerEntryKind type,
                                       BigDecimal amount, PaymentChannel channel) {
        super(reference, null);
        this.reference = reference;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.channel = channel;
    }

    public String reference() {
        return reference;
    }

    public long employeeId() {
        return employeeId;
    }

    public String employeeName() {
        return employeeName;
    }

    public LocalDate date() {
        return date;
    }

    public LedgerEntryKind type() {
        return type;
    }

    public BigDecimal amount() {
        return amount;
    }

    public PaymentChannel channel() {
        return channel;
    }

    @Override
    public String eventName() {
        return "employees.ledger-entry-recorded";
    }

    @Override
    public Map<String, Object> payload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reference", reference);
        payload.put("employeeId", employeeId);
        payload.put("employeeName", employeeName);
        payload.put("date", date.toString());
        payload.put("type", type.name());
        payload.put("amount", amount.toPlainString());
        payload.put("channel", channel == null ? null : channel.name());
        return payload;
    }
}
