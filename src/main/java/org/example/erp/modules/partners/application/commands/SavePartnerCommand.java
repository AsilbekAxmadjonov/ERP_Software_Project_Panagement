package org.example.erp.modules.partners.application.commands;

import org.example.erp.modules.partners.application.enums.PartnerKind;

public record SavePartnerCommand(String name, PartnerKind type, String contactPerson, String phone, String email,
                                 String address, String taxId, boolean active) {}
