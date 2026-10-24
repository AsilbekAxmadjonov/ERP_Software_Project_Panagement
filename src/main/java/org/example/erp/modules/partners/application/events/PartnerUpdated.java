package org.example.erp.modules.partners.application.events;

import org.example.erp.modules.partners.application.dto.PartnerDTO;
import org.example.erp.platform.BaseIntegrationEvent;

import java.util.Map;

/** Carries the partner's state after the edit. */
public final class PartnerUpdated extends BaseIntegrationEvent {
    private final PartnerDTO partner;

    public PartnerUpdated(PartnerDTO partner) {
        super(null, null);
        this.partner = partner;
    }

    public PartnerDTO partner() {
        return partner;
    }

    @Override
    public String eventName() {
        return "partners.partner-updated";
    }

    @Override
    public Map<String, Object> payload() {
        return PartnerPayload.of(partner);
    }
}
