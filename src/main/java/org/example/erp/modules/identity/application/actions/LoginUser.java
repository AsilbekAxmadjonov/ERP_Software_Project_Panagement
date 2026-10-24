package org.example.erp.modules.identity.application.actions;

import org.example.erp.modules.identity.application.contracts.PasswordHasher;
import org.example.erp.modules.identity.application.contracts.TokenService;
import org.example.erp.modules.identity.application.dto.LoginResultDTO;
import org.example.erp.modules.identity.application.events.LoginFailed;
import org.example.erp.modules.identity.application.events.LoginSucceeded;
import org.example.erp.modules.identity.domain.UserAccount;
import org.example.erp.modules.identity.domain.UserAccountRepository;
import org.example.erp.platform.AuthenticationFailedException;
import org.example.erp.platform.IntegrationEventPublisher;

import java.util.Optional;

/**
 * Not transactional on purpose: a failed attempt must still be recorded by listeners even though the login is
 * rejected with an exception. The caller always gets the same message, whatever the reason.
 */
public final class LoginUser {
    private static final int MAX_USERNAME = 100;

    private final UserAccountRepository users;
    private final PasswordHasher hasher;
    private final TokenService tokens;
    private final IntegrationEventPublisher events;

    public LoginUser(UserAccountRepository users, PasswordHasher hasher, TokenService tokens, IntegrationEventPublisher events) {
        this.users = users;
        this.hasher = hasher;
        this.tokens = tokens;
        this.events = events;
    }

    public LoginResultDTO handle(String username, String password) {
        String name = username.trim();
        Optional<UserAccount> found = users.findByUsername(name);
        String failure = found.isEmpty() ? LoginFailed.UNKNOWN_USER
                : !found.get().isActive() ? LoginFailed.INACTIVE_USER
                : !hasher.matches(password, found.get().getPasswordHash()) ? LoginFailed.WRONG_PASSWORD
                : null;
        if (failure != null) {
            events.publish(new LoginFailed(name.length() > MAX_USERNAME ? name.substring(0, MAX_USERNAME) : name, failure));
            throw new AuthenticationFailedException("Invalid username or password");
        }
        UserAccount user = found.get();
        events.publish(new LoginSucceeded(user.getUsername()));
        return new LoginResultDTO(tokens.issue(user.getUsername()), UserMapper.toDto(user));
    }
}
