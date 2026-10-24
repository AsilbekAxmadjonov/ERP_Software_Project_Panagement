package org.example.erp.modules.audit.presentation;

import org.example.erp.modules.audit.application.actions.ListAuditEntries;
import org.example.erp.modules.audit.application.dto.AuditEntryDTO;
import org.example.erp.modules.audit.application.queries.AuditQuery;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
class AuditController {
    private final ListAuditEntries listEntries;
    private final Clock clock;

    AuditController(ListAuditEntries listEntries, Clock clock) {
        this.listEntries = listEntries;
        this.clock = clock;
    }

    /** from and to are inclusive calendar days in the server's time zone. */
    @GetMapping
    List<AuditEntryDTO> list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                             @RequestParam(required = false) String actor,
                             @RequestParam(required = false) String action,
                             @RequestParam(required = false) Integer limit) {
        return listEntries.handle(new AuditQuery(
                from == null ? null : from.atStartOfDay(clock.getZone()).toInstant(),
                to == null ? null : to.plusDays(1).atStartOfDay(clock.getZone()).toInstant(),
                actor, action, limit));
    }
}
