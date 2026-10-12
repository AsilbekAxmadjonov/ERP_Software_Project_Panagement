package org.example.erp.modules.accounting.infrastructure;

import org.example.erp.modules.accounting.domain.JournalEntry;
import org.example.erp.modules.accounting.domain.JournalEntryRepository;
import org.example.erp.modules.accounting.domain.JournalLine;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaJournalEntryRepository implements JournalEntryRepository {
    private final JournalEntryJpaRepository jpa;

    JpaJournalEntryRepository(JournalEntryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<JournalEntry> findById(long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<JournalEntry> findRecent() {
        return jpa.findTop200ByOrderByEntryDateDescIdDesc().stream().map(this::toDomain).toList();
    }

    @Override
    public JournalEntry save(JournalEntry e) {
        // lines of a posted entry are immutable, so an existing record only has its reversed flag updated
        JournalEntryRecord r = e.getId() == null ? new JournalEntryRecord() : jpa.getReferenceById(e.getId());
        r.setEntryDate(e.getEntryDate());
        r.setReference(e.getReference());
        r.setDescription(e.getDescription());
        r.setOrigin(e.getOrigin());
        r.setReversed(e.isReversed());
        r.setReversalOf(e.getReversalOf());
        r.setCreatedBy(e.getCreatedBy());
        if (e.getId() == null) {
            for (JournalLine l : e.getLines()) {
                JournalLineRecord lr = new JournalLineRecord();
                lr.setEntry(r);
                lr.setAccountCode(l.accountCode());
                lr.setPartnerId(l.partnerId());
                lr.setDebit(l.debit());
                lr.setCredit(l.credit());
                lr.setMemo(l.memo());
                r.getLines().add(lr);
            }
        }
        return toDomain(jpa.save(r));
    }

    private JournalEntry toDomain(JournalEntryRecord r) {
        List<JournalLine> lines = r.getLines().stream()
                .map(l -> new JournalLine(l.getAccountCode(), l.getDebit(), l.getCredit(), l.getPartnerId(), l.getMemo()))
                .toList();
        return JournalEntry.restore(r.getId(), r.getEntryDate(), r.getReference(), r.getDescription(), r.getOrigin(),
                r.isReversed(), r.getReversalOf(), r.getCreatedBy(), lines);
    }
}
