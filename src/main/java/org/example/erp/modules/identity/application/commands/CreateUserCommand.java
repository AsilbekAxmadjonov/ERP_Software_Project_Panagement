package org.example.erp.modules.identity.application.commands;

import org.example.erp.modules.identity.application.enums.UserRole;

public record CreateUserCommand(String username, String password, String fullName, UserRole role) {}
