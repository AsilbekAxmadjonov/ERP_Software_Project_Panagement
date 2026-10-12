package org.example.erp.modules.identity.application.dto;

import org.example.erp.modules.identity.application.enums.UserRole;

public record UserDTO(long id, String username, String fullName, UserRole role, boolean active) {}
