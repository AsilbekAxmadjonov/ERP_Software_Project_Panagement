package org.example.erp.modules.audit.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import org.example.erp.modules.audit.domain.AuditEntry;
import org.example.erp.modules.audit.domain.AuditEntryRepository;
import org.example.erp.modules.audit.domain.AuditFilter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
class JpaAuditEntryRepository implements AuditEntryRepository {
    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final AuditEntryJpaRepository jpa;
    private final ObjectMapper json;

    JpaAuditEntryRepository(AuditEntryJpaRepository jpa, ObjectMapper json) {
        this.jpa = jpa;
        this.json = json;
    }

    @Override
    public AuditEntry save(AuditEntry e) {
        AuditEntryRecord r = new AuditEntryRecord();
        r.setOccurredAt(e.getOccurredAt());
        r.setActor(e.getActor());
        r.setAction(e.getAction());
        r.setSummary(e.getSummary());
        r.setSubject(e.getSubject());
        r.setPayload(toJson(e.getPayload()));
        return toDomain(jpa.save(r));
    }

    @Override
    public List<AuditEntry> search(AuditFilter f) {
        Specification<AuditEntryRecord> spec = (root, query, cb) -> {
            List<Predicate> where = new ArrayList<>();
            if (f.from() != null) where.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), f.from()));
            if (f.to() != null) where.add(cb.lessThan(root.get("occurredAt"), f.to()));
            if (f.actor() != null) where.add(cb.equal(cb.lower(root.get("actor")), f.actor().toLowerCase()));
            if (f.action() != null) where.add(cb.or(cb.equal(root.get("action"), f.action()),
                    cb.like(root.get("action"), escapeLike(f.action()) + ".%", '\\')));
            return cb.and(where.toArray(Predicate[]::new));
        };
        PageRequest page = PageRequest.of(0, f.limit(), Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.desc("id")));
        return jpa.findAll(spec, page).stream().map(this::toDomain).toList();
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private String toJson(Map<String, Object> payload) {
        try {
            String text = json.writeValueAsString(payload);
            if (text.length() <= AuditEntryRecord.MAX_PAYLOAD) return text;
            return json.writeValueAsString(Map.of("truncated", true, "keys", List.copyOf(payload.keySet())));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize audit payload", e);
        }
    }

    private AuditEntry toDomain(AuditEntryRecord r) {
        try {
            return AuditEntry.restore(r.getId(), r.getOccurredAt(), r.getActor(), r.getAction(), r.getSummary(),
                    r.getSubject(), json.readValue(r.getPayload(), MAP));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Corrupt audit payload in entry " + r.getId(), e);
        }
    }
}
