package org.example.erp.modules.partners.application.events;

import org.example.erp.modules.partners.application.dto.PartnerDTO;

import java.util.LinkedHashMap;
import java.util.Map;

final class PartnerPayload {
    private PartnerPayload() {}

    static Map<String, Object> of(PartnerDTO p) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("partnerId", p.id());
        payload.put("partnerName", p.name());
        payload.put("type", p.type().name());
        payload.put("customer", p.customer());
        payload.put("supplier", p.supplier());
        payload.put("taxId", p.taxId());
        payload.put("active", p.active());
        return payload;
    }
}
