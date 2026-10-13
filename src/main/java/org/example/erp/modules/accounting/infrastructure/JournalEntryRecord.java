package org.example.erp.modules.accounting.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.erp.modules.accounting.domain.EntryOrigin;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounting_journal_entries")
@Getter @Setter
class JournalEntryRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate entryDate;
    private String reference;
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private EntryOrigin origin;
    private boolean reversed;
    private Long reversalOf;
    private String createdBy;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<JournalLineRecord> lines = new ArrayList<>();
}
