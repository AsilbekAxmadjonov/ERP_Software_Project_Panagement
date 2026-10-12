package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.contracts.LedgerReadModel;
import org.example.erp.modules.accounting.application.dto.LedgerRowDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Balance is debit minus credit: positive means the partner owes us, negative means we owe them. */
public final class GetPartnerLedger {
    private final LedgerReadModel ledger;

    public GetPartnerLedger(LedgerReadModel ledger) {
        this.ledger = ledger;
    }

    public List<LedgerRowDTO> handle(long partnerId) {
        BigDecimal balance = BigDecimal.ZERO;
        List<LedgerRowDTO> rows = new ArrayList<>();
        for (LedgerReadModel.LedgerLine l : ledger.partnerLines(partnerId)) {
            balance = balance.add(l.debit().subtract(l.credit()));
            rows.add(new LedgerRowDTO(l.entryId(), l.date(), l.reference(), l.description(), l.debit(), l.credit(), balance));
        }
        return rows;
    }
}
