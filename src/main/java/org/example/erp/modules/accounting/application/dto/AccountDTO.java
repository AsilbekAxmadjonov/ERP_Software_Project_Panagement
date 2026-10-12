package org.example.erp.modules.accounting.application.dto;

import org.example.erp.modules.accounting.application.enums.AccountKind;

public record AccountDTO(long id, String code, String name, AccountKind type, boolean active) {}
