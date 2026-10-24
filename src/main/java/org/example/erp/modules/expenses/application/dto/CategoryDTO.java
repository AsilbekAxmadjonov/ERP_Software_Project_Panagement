package org.example.erp.modules.expenses.application.dto;

import org.example.erp.modules.expenses.application.enums.EntryType;

public record CategoryDTO(long id, String name, EntryType type, String accountCode, boolean active) {}
