package org.example.erp.modules.accounting.application.contracts;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LedgerReadModel {
    record AccountTotals(String accountCode, BigDecimal debit, BigDecimal credit) {}

    record LedgerLine(long entryId, LocalDate date, String reference, String description, BigDecimal debit, BigDecimal credit) {}

    List<AccountTotals> totalsByAccount();

    List<LedgerLine> accountLines(String accountCode);

    List<LedgerLine> partnerLines(long partnerId);

    BigDecimal partnerBalance(long partnerId, String accountCode);
}
