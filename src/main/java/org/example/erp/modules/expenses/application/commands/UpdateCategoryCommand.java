package org.example.erp.modules.expenses.application.commands;

/** A category's type is fixed at creation; its name, account and archived state can change. */
public record UpdateCategoryCommand(String name, String accountCode, boolean active) {}
