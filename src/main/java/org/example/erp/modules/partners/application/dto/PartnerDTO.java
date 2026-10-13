package org.example.erp.modules.partners.application.dto;

import org.example.erp.modules.partners.application.enums.PartnerKind;

public record PartnerDTO(long id, String name, PartnerKind type, String contactPerson, String phone, String email,
                         String address, String taxId, boolean active, boolean customer, boolean supplier) {}
