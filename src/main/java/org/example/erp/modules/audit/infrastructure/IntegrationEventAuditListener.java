package org.example.erp.modules.audit.infrastructure;

import org.example.erp.modules.audit.application.actions.RecordAuditEntry;
import org.example.erp.modules.audit.application.commands.RecordAuditEntryCommand;
import org.example.erp.platform.CurrentActor;
import org.example.erp.platform.IntegrationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Records every integration event, from any module, by listening to the platform type only; new modules are audited
 * without any change here.
 * <p>
 * The write happens synchronously in the publisher's transaction on purpose: an entry exists exactly when the
 * business change committed, and a change can never commit without its audit entry. The cost is that a failing audit
 * write rolls the business action back, which is the right trade-off for an audit trail. Events published outside a
 * transaction (a failed login) are saved in their own transaction by the repository.
 */
@Component
class IntegrationEventAuditListener {
    private final RecordAuditEntry record;
    private final CurrentActor currentActor;

    IntegrationEventAuditListener(RecordAuditEntry record, CurrentActor currentActor) {
        this.record = record;
        this.currentActor = currentActor;
    }

    @EventListener
    void on(IntegrationEvent event) {
        String actor = event.actor() != null ? event.actor() : currentActor.username();
        record.handle(new RecordAuditEntryCommand(event.occurredAt(), actor, event.eventName(), event.payload()));
    }
}
