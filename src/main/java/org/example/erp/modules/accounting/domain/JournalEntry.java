package org.example.erp.modules.accounting.domain;

import lombok.Getter;
import org.example.erp.platform.BusinessRuleException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Posted entries are never edited or deleted; a mistake is cancelled by a reversal entry. */
@Getter
public final class JournalEntry {
    private final Long id;
    private final LocalDate entryDate;
    private final String reference;
    private final String description;
    private final EntryOrigin origin;
    private boolean reversed;
    private final Long reversalOf;
    private final String createdBy;
    private final List<JournalLine> lines;

    private JournalEntry(Long id, LocalDate entryDate, String reference, String description, EntryOrigin origin,
                         boolean reversed, Long reversalOf, String createdBy, List<JournalLine> lines) {
        this.id = id;
        this.entryDate = entryDate;
        this.reference = reference;
        this.description = description;
        this.origin = origin;
        this.reversed = reversed;
        this.reversalOf = reversalOf;
        this.createdBy = createdBy;
        this.lines = lines;
    }

    public static JournalEntry post(LocalDate date, String reference, String description, EntryOrigin origin,
                                    String createdBy, List<JournalLine> lines) {
        return create(date, reference, description, origin, null, createdBy, lines);
    }

    public static JournalEntry restore(Long id, LocalDate entryDate, String reference, String description, EntryOrigin origin,
                                       boolean reversed, Long reversalOf, String createdBy, List<JournalLine> lines) {
        return new JournalEntry(id, entryDate, reference, description, origin, reversed, reversalOf, createdBy, List.copyOf(lines));
    }

    public JournalEntry reversal(LocalDate date, String createdBy) {
        if (reversed) throw new BusinessRuleException("Entry is already reversed");
        if (origin != EntryOrigin.MANUAL)
            throw new BusinessRuleException("Only manual entries can be reversed here; " + origin + " entries belong to their source document");
        List<JournalLine> swapped = lines.stream().map(l -> l.swapped("Reversal")).toList();
        return create(date, "REV-" + id, "Reversal of entry #" + id, EntryOrigin.REVERSAL, id, createdBy, swapped);
    }

    public void markReversed() {
        this.reversed = true;
    }

    public BigDecimal total() {
        return lines.stream().map(JournalLine::debit).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static JournalEntry create(LocalDate date, String reference, String description, EntryOrigin origin,
                                       Long reversalOf, String createdBy, List<JournalLine> lines) {
        if (lines.size() < 2) throw new BusinessRuleException("A journal entry needs at least two lines");
        BigDecimal debit = lines.stream().map(JournalLine::debit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credit = lines.stream().map(JournalLine::credit).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (debit.compareTo(credit) != 0)
            throw new BusinessRuleException("Entry is not balanced: debit " + debit + " vs credit " + credit);
        return new JournalEntry(null, date, reference, description, origin, false, reversalOf, createdBy, List.copyOf(lines));
    }
}
