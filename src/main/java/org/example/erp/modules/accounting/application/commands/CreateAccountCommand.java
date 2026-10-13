package org.example.erp.modules.accounting.application.commands;

import org.example.erp.modules.accounting.application.enums.AccountKind;

public record CreateAccountCommand(String code, String name, AccountKind type) {}
