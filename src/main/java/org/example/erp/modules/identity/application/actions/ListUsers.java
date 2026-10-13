package org.example.erp.modules.identity.application.actions;

import org.example.erp.modules.identity.application.dto.UserDTO;
import org.example.erp.modules.identity.domain.UserAccountRepository;

import java.util.List;

public final class ListUsers {
    private final UserAccountRepository users;

    public ListUsers(UserAccountRepository users) {
        this.users = users;
    }

    public List<UserDTO> handle() {
        return users.findAll().stream().map(UserMapper::toDto).toList();
    }
}
