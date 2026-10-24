package org.example.erp.modules.accounting.application.actions;

import org.example.erp.modules.accounting.application.dto.PeriodLockDTO;
import org.example.erp.modules.accounting.domain.PeriodLockRepository;

public final class GetPeriodLock {
    private final PeriodLockRepository periods;

    public GetPeriodLock(PeriodLockRepository periods) {
        this.periods = periods;
    }

    public PeriodLockDTO handle() {
        return AccountingMapper.toDto(periods.get());
    }
}
