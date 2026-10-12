package org.example.erp.modules.accounting.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.erp.modules.accounting.application.contracts.LedgerReadModel;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@Transactional(readOnly = true)
class JpaLedgerReadModel implements LedgerReadModel {
    private static final String LINES = "select e.id, e.entryDate, e.reference, e.description, l.memo, l.debit, l.credit "
            + "from JournalLineRecord l join l.entry e where %s order by e.entryDate, l.id";

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<AccountTotals> totalsByAccount() {
        return em.createQuery("select l.accountCode, sum(l.debit), sum(l.credit) from JournalLineRecord l group by l.accountCode", Object[].class)
                .getResultList().stream()
                .map(r -> new AccountTotals((String) r[0], (BigDecimal) r[1], (BigDecimal) r[2]))
                .toList();
    }

    @Override
    public List<LedgerLine> accountLines(String accountCode) {
        return em.createQuery(LINES.formatted("l.accountCode = :key"), Object[].class)
                .setParameter("key", accountCode).getResultList().stream().map(this::toLine).toList();
    }

    @Override
    public List<LedgerLine> partnerLines(long partnerId) {
        return em.createQuery(LINES.formatted("l.partnerId = :key"), Object[].class)
                .setParameter("key", partnerId).getResultList().stream().map(this::toLine).toList();
    }

    @Override
    public BigDecimal partnerBalance(long partnerId, String accountCode) {
        return em.createQuery("select coalesce(sum(l.debit - l.credit), 0) from JournalLineRecord l "
                        + "where l.partnerId = :partner and l.accountCode = :code", BigDecimal.class)
                .setParameter("partner", partnerId).setParameter("code", accountCode).getSingleResult();
    }

    private LedgerLine toLine(Object[] r) {
        String memo = (String) r[4];
        return new LedgerLine((Long) r[0], (LocalDate) r[1], (String) r[2], memo != null ? memo : (String) r[3],
                (BigDecimal) r[5], (BigDecimal) r[6]);
    }
}
