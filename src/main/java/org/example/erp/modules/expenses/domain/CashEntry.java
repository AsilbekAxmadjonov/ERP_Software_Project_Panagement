package org.example.erp.modules.expenses.domain;

import lombok.Getter;
import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An expense paid or other income received in cash or through the bank. Once recorded it is posted; a mistake is
 * undone by cancelling it, which keeps the record and posts a reversing ledger entry on the cancellation date.
 */
@Getter
public final class CashEntry {
    private final Long id;
    private final CategoryKind kind;
    private final long categoryId;
    private final String categoryName;
    private final String accountCode;
    private final LocalDate date;
    private final BigDecimal amount;
    private final PaymentMethod method;
    private final String description;
    private EntryStatus status;
    private LocalDate cancelledOn;
    private final String createdBy;

    private CashEntry(Long id, CategoryKind kind, long categoryId, String categoryName, String accountCode, LocalDate date,
                      BigDecimal amount, PaymentMethod method, String description, EntryStatus status, LocalDate cancelledOn,
                      String createdBy) {
        this.id = id;
        this.kind = kind;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.accountCode = accountCode;
        this.date = date;
        this.amount = amount;
        this.method = method;
        this.description = description;
        this.status = status;
        this.cancelledOn = cancelledOn;
        this.createdBy = createdBy;
    }

    /** The category's current name and account are copied, so later category edits do not rewrite history. */
    public static CashEntry record(CategoryKind kind, Category category, LocalDate date, BigDecimal amount, PaymentMethod method,
                                   String description, String createdBy) {
        if (category.getKind() != kind) {
            String expected = switch (kind) {
                case EXPENSE -> "an expense";
                case INCOME -> "an income";
            };
            throw new BusinessRuleException(category.getName() + " is not " + expected + " category");
        }
        if (!category.isActive()) throw new BusinessRuleException("Category " + category.getName() + " is archived");
        if (amount.signum() <= 0) throw new BusinessRuleException("Amount must be greater than zero");
        String text = description == null || description.isBlank() ? null : description.trim();
        return new CashEntry(null, kind, category.getId(), category.getName(), category.getAccountCode(), date, amount, method,
                text, EntryStatus.POSTED, null, createdBy);
    }

    public static CashEntry restore(Long id, CategoryKind kind, long categoryId, String categoryName, String accountCode,
                                    LocalDate date, BigDecimal amount, PaymentMethod method, String description,
                                    EntryStatus status, LocalDate cancelledOn, String createdBy) {
        return new CashEntry(id, kind, categoryId, categoryName, accountCode, date, amount, method, description, status,
                cancelledOn, createdBy);
    }

    public void cancel(LocalDate on) {
        if (status == EntryStatus.CANCELLED) throw new BusinessRuleException(reference() + " is already cancelled");
        if (on.isBefore(date)) throw new BusinessRuleException("A cancellation cannot be dated before the entry itself");
        this.status = EntryStatus.CANCELLED;
        this.cancelledOn = on;
    }

    /** Document number such as EXP-00012; only known once the entry has been saved. */
    public String reference() {
        if (id == null) throw new IllegalStateException("An unsaved entry has no reference yet");
        return String.format("%s-%05d", kind.referencePrefix(), id);
    }
}
