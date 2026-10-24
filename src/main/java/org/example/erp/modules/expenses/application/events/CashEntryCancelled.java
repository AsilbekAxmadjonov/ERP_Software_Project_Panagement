package org.example.erp.modules.expenses.application.events;

import org.example.erp.modules.expenses.application.enums.EntryType;
import org.example.erp.modules.expenses.application.enums.PaymentChannel;
import org.example.erp.platform.BaseIntegrationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** A recorded expense or income was cancelled; what it posted should be reversed as of cancelledOn. */
public final class CashEntryCancelled extends BaseIntegrationEvent {
    private final String reference;
    private final EntryType type;
    private final LocalDate cancelledOn;
    private final BigDecimal amount;
    private final PaymentChannel channel;
    private final String accountCode;
    private final String categoryName;

    public CashEntryCancelled(String reference, EntryType type, LocalDate cancelledOn, BigDecimal amount, PaymentChannel channel,
                              String accountCode, String categoryName) {
        super(reference, null);
        this.reference = reference;
        this.type = type;
        this.cancelledOn = cancelledOn;
        this.amount = amount;
        this.channel = channel;
        this.accountCode = accountCode;
        this.categoryName = categoryName;
    }

    public String reference() {
        return reference;
    }

    public EntryType type() {
        return type;
    }

    public LocalDate cancelledOn() {
        return cancelledOn;
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

    @Override
    public String eventName() {
        return "expenses.entry-cancelled";
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of("reference", reference, "type", type.name(), "cancelledOn", cancelledOn.toString(),
                "amount", amount.toPlainString(), "channel", channel.name(), "accountCode", accountCode,
                "categoryName", categoryName);
    }
}
