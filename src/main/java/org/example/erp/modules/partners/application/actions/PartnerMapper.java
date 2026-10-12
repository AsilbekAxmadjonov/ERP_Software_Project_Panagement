package org.example.erp.modules.partners.application.actions;

import org.example.erp.modules.partners.application.dto.PartnerDTO;
import org.example.erp.modules.partners.application.enums.PartnerKind;
import org.example.erp.modules.partners.domain.Partner;
import org.example.erp.modules.partners.domain.PartnerType;

final class PartnerMapper {
    private PartnerMapper() {}

    static PartnerDTO toDto(Partner p) {
        return new PartnerDTO(p.getId(), p.getName(), PartnerKind.valueOf(p.getType().name()), p.getContactPerson(),
                p.getPhone(), p.getEmail(), p.getAddress(), p.getTaxId(), p.isActive(), p.isCustomer(), p.isSupplier());
    }

    static PartnerType toDomain(PartnerKind kind) {
        return PartnerType.valueOf(kind.name());
    }
}
