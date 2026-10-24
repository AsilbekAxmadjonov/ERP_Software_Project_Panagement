package org.example.erp.modules.accounting.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.erp.modules.accounting.application.contracts.LedgerReadModel;
import org.example.erp.modules.accounting.application.enums.EntryKind;
import org.example.erp.modules.accounting.domain.EntryOrigin;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
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
    public List<AccountTotals> totalsByAccount(LocalDate from, LocalDate to) {
        TypedQuery<Object[]> query = em.createQuery("select l.accountCode, sum(l.debit), sum(l.credit) from JournalLineRecord l join l.entry e "
                + "where " + dateRange(from, to) + " group by l.accountCode", Object[].class);
        return withDates(query, from, to).getResultList().stream()
                .map(r -> new AccountTotals((String) r[0], (BigDecimal) r[1], (BigDecimal) r[2]))
                .toList();
    }

    @Override
    public List<MovementLine> movementLines(Collection<String> accountCodes, LocalDate from, LocalDate to) {
        if (accountCodes.isEmpty()) return List.of();
        TypedQuery<Object[]> query = em.createQuery("select e.id, e.entryDate, e.reference, e.description, e.origin, l.accountCode, "
                + "l.debit, l.credit, l.memo from JournalLineRecord l join l.entry e where l.accountCode in :codes and "
                + dateRange(from, to) + " order by e.entryDate, e.id, l.id", Object[].class);
        return withDates(query.setParameter("codes", accountCodes), from, to).getResultList().stream()
                .map(r -> new MovementLine((Long) r[0], (LocalDate) r[1], (String) r[2], r[3] != null ? (String) r[3] : (String) r[8],
                        EntryKind.valueOf(((EntryOrigin) r[4]).name()), (String) r[5], (BigDecimal) r[6], (BigDecimal) r[7]))
                .toList();
    }

    private static String dateRange(LocalDate from, LocalDate to) {
        return (from == null ? "1 = 1" : "e.entryDate >= :from") + (to == null ? "" : " and e.entryDate <= :to");
    }

    private static TypedQuery<Object[]> withDates(TypedQuery<Object[]> query, LocalDate from, LocalDate to) {
        if (from != null) query.setParameter("from", from);
        if (to != null) query.setParameter("to", to);
        return query;
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
