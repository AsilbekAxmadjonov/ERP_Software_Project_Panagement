package org.example.erp.modules.identity.application.actions;

import org.example.erp.modules.identity.application.dto.UserDTO;
import org.example.erp.modules.identity.domain.UserAccountRepository;
import org.example.erp.platform.NotFoundException;

public final class GetUserByUsername {
    private final UserAccountRepository users;

    public GetUserByUsername(UserAccountRepository users) {
        this.users = users;
    }

    public UserDTO handle(String username) {
        return users.findByUsername(username).map(UserMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
