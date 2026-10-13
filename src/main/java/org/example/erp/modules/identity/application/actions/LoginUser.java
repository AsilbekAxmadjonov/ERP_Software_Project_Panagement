package org.example.erp.modules.identity.application.actions;

import org.example.erp.modules.identity.application.contracts.PasswordHasher;
import org.example.erp.modules.identity.application.contracts.TokenService;
import org.example.erp.modules.identity.application.dto.LoginResultDTO;
import org.example.erp.modules.identity.domain.UserAccount;
import org.example.erp.modules.identity.domain.UserAccountRepository;
import org.example.erp.platform.AuthenticationFailedException;

public final class LoginUser {
    private final UserAccountRepository users;
    private final PasswordHasher hasher;
    private final TokenService tokens;

    public LoginUser(UserAccountRepository users, PasswordHasher hasher, TokenService tokens) {
        this.users = users;
        this.hasher = hasher;
        this.tokens = tokens;
    }

    public LoginResultDTO handle(String username, String password) {
        UserAccount user = users.findByUsername(username.trim())
                .filter(u -> u.isActive() && hasher.matches(password, u.getPasswordHash()))
                .orElseThrow(() -> new AuthenticationFailedException("Invalid username or password"));
        return new LoginResultDTO(tokens.issue(user.getUsername()), UserMapper.toDto(user));
    }
}
