package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.contracts.LedgerReadModel;
import org.example.erp.modules.accounting.application.enums.LedgerRole;

import java.math.BigDecimal;

public final class GetPartnerBalance {
    private final LedgerReadModel ledger;

    public GetPartnerBalance(LedgerReadModel ledger) {
        this.ledger = ledger;
    }

    public BigDecimal handle(long partnerId, LedgerRole role) {
        return ledger.partnerBalance(partnerId, role.code());
    }
}
