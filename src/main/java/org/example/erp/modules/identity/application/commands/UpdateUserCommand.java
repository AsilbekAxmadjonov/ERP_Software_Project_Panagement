package org.example.erp.modules.identity.application.commands;

import org.example.erp.modules.identity.application.enums.UserRole;

/** Null fields are left unchanged. */
public record UpdateUserCommand(long id, String fullName, UserRole role, Boolean active, String password) {}
