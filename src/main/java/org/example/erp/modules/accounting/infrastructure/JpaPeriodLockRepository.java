package org.example.erp.modules.accounting.infrastructure;

import org.example.erp.modules.accounting.domain.PeriodLock;
import org.example.erp.modules.accounting.domain.PeriodLockRepository;
import org.springframework.stereotype.Repository;

/** One row (id 1) holds the company-wide lock. */
@Repository
class JpaPeriodLockRepository implements PeriodLockRepository {
    private static final long ID = 1L;
    private final PeriodLockJpaRepository jpa;

    JpaPeriodLockRepository(PeriodLockJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public PeriodLock get() {
        return jpa.findById(ID).map(r -> PeriodLock.restore(r.getLockDate(), r.getChangedBy(), r.getChangedAt()))
                .orElseGet(PeriodLock::open);
    }

    @Override
    public PeriodLock save(PeriodLock lock) {
        PeriodLockRecord r = jpa.findById(ID).orElseGet(() -> {
            PeriodLockRecord created = new PeriodLockRecord();
            created.setId(ID);
            return created;
        });
        r.setLockDate(lock.getLockDate());
        r.setChangedBy(lock.getChangedBy());
        r.setChangedAt(lock.getChangedAt());
        jpa.save(r);
        return lock;
    }
}
