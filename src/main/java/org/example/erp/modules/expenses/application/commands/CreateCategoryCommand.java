package org.example.erp.modules.expenses.application.commands;

import org.example.erp.modules.expenses.application.enums.EntryType;

public record CreateCategoryCommand(String name, EntryType type, String accountCode) {}
