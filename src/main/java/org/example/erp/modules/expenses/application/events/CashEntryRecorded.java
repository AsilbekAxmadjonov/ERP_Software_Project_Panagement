package org.example.erp.modules.expenses.application.events;

import org.example.erp.modules.expenses.application.enums.EntryType;
import org.example.erp.modules.expenses.application.enums.PaymentChannel;
import org.example.erp.platform.BaseIntegrationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/** An expense was paid or other income received; accountCode is the category's expense or revenue account. */
public final class CashEntryRecorded extends BaseIntegrationEvent {
    private final String reference;
    private final EntryType type;
    private final LocalDate date;
    private final BigDecimal amount;
    private final PaymentChannel channel;
    private final String accountCode;
    private final String categoryName;
    private final String description;

    public CashEntryRecorded(String reference, EntryType type, LocalDate date, BigDecimal amount, PaymentChannel channel,
                             String accountCode, String categoryName, String description) {
        super(reference, null);
        this.reference = reference;
        this.type = type;
        this.date = date;
        this.amount = amount;
        this.channel = channel;
        this.accountCode = accountCode;
        this.categoryName = categoryName;
        this.description = description;
    }

    public String reference() {
        return reference;
    }

    public EntryType type() {
        return type;
    }

    public LocalDate date() {
        return date;
    }

    public BigDecimal amount() {
        return amount;
    }

    public PaymentChannel channel() {
        return channel;
    }

    public String accountCode() {
        return accountCode;
    }

    public String categoryName() {
        return categoryName;
    }

    public String description() {
        return description;
    }

    @Override
    public String eventName() {
        return "expenses.entry-recorded";
    }

    @Override
    public Map<String, Object> payload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reference", reference);
        payload.put("type", type.name());
        payload.put("date", date.toString());
        payload.put("amount", amount.toPlainString());
        payload.put("channel", channel.name());
        payload.put("accountCode", accountCode);
        payload.put("categoryName", categoryName);
        payload.put("description", description);
        return payload;
    }
}
