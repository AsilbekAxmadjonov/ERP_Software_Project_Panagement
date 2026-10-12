package org.example.erp.modules.identity.application.actions;

import org.example.erp.modules.identity.application.contracts.TokenService;
import org.example.erp.modules.identity.application.dto.PrincipalDTO;
import org.example.erp.modules.identity.application.enums.UserRole;
import org.example.erp.modules.identity.domain.UserAccountRepository;

import java.util.Optional;

public final class AuthenticateToken {
    private final UserAccountRepository users;
    private final TokenService tokens;

    public AuthenticateToken(UserAccountRepository users, TokenService tokens) {
        this.users = users;
        this.tokens = tokens;
    }

    public Optional<PrincipalDTO> handle(String token) {
        return tokens.usernameFrom(token)
                .flatMap(users::findByUsername)
                .filter(u -> u.isActive())
                .map(u -> new PrincipalDTO(u.getUsername(), UserRole.valueOf(u.getRole().name())));
    }
}
