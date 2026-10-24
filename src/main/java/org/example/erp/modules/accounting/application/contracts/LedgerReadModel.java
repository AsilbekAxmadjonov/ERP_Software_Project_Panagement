package org.example.erp.modules.accounting.application.contracts;

import org.example.erp.modules.accounting.application.enums.EntryKind;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface LedgerReadModel {
    record AccountTotals(String accountCode, BigDecimal debit, BigDecimal credit) {}

    record LedgerLine(long entryId, LocalDate date, String reference, String description, BigDecimal debit, BigDecimal credit) {}

    /** A journal line together with the origin of its entry, for reports that group movements by source document. */
    record MovementLine(long entryId, LocalDate date, String reference, String description, EntryKind origin,
                        String accountCode, BigDecimal debit, BigDecimal credit) {}

    List<AccountTotals> totalsByAccount();

    /** Totals of entries dated within [from, to]; a null bound is open. */
    List<AccountTotals> totalsByAccount(LocalDate from, LocalDate to);

    List<LedgerLine> accountLines(String accountCode);

    /** Lines of the given accounts dated within [from, to] in posting order; a null bound is open. */
    List<MovementLine> movementLines(Collection<String> accountCodes, LocalDate from, LocalDate to);

    List<LedgerLine> partnerLines(long partnerId);

    BigDecimal partnerBalance(long partnerId, String accountCode);
}
