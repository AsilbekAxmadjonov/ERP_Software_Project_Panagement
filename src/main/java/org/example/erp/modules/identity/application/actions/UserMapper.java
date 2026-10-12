package org.example.erp.modules.identity.application.actions;

import org.example.erp.modules.identity.application.dto.UserDTO;
import org.example.erp.modules.identity.application.enums.UserRole;
import org.example.erp.modules.identity.domain.Role;
import org.example.erp.modules.identity.domain.UserAccount;

final class UserMapper {
    private UserMapper() {}

    static UserDTO toDto(UserAccount u) {
        return new UserDTO(u.getId(), u.getUsername(), u.getFullName(), UserRole.valueOf(u.getRole().name()), u.isActive());
    }

    static Role toDomain(UserRole role) {
        return Role.valueOf(role.name());
    }
}
