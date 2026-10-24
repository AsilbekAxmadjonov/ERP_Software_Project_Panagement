package org.example.erp.modules.audit.infrastructure;

import org.example.erp.modules.audit.application.actions.ListAuditEntries;
import org.example.erp.modules.audit.application.actions.RecordAuditEntry;
import org.example.erp.modules.audit.domain.AuditEntryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditModuleConfig {

    @Bean
    RecordAuditEntry recordAuditEntry(AuditEntryRepository entries) {
        return new RecordAuditEntry(entries);
    }

    @Bean
    ListAuditEntries listAuditEntries(AuditEntryRepository entries) {
        return new ListAuditEntries(entries);
    }
}
