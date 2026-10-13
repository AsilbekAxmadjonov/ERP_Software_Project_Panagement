package org.example.erp.modules.identity.application.dto;

import org.example.erp.modules.identity.application.enums.UserRole;

public record PrincipalDTO(String username, UserRole role) {}
