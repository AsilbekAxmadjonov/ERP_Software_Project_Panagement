package org.example.erp.modules.identity.infrastructure;

import org.example.erp.modules.identity.application.actions.*;
import org.example.erp.modules.identity.application.contracts.PasswordHasher;
import org.example.erp.modules.identity.application.contracts.TokenService;
import org.example.erp.modules.identity.application.enums.UserRole;
import org.example.erp.modules.identity.application.commands.CreateUserCommand;
import org.example.erp.modules.identity.domain.UserAccountRepository;
import org.example.erp.platform.TransactionRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityModuleConfig {
    private static final Logger log = LoggerFactory.getLogger(IdentityModuleConfig.class);

    @Bean
    PasswordHasher passwordHasher() {
        return new BcryptPasswordHasher();
    }

    @Bean
    TokenService tokenService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.ttl-hours}") long ttlHours) {
        return new JwtTokenService(secret, ttlHours);
    }

    @Bean
    LoginUser loginUser(UserAccountRepository users, PasswordHasher hasher, TokenService tokens) {
        return new LoginUser(users, hasher, tokens);
    }

    @Bean
    public AuthenticateToken authenticateToken(UserAccountRepository users, TokenService tokens) {
        return new AuthenticateToken(users, tokens);
    }

    @Bean
    GetUserByUsername getUserByUsername(UserAccountRepository users) {
        return new GetUserByUsername(users);
    }

    @Bean
    ListUsers listUsers(UserAccountRepository users) {
        return new ListUsers(users);
    }

    @Bean
    CreateUser createUser(UserAccountRepository users, PasswordHasher hasher, TransactionRunner tx) {
        return new CreateUser(users, hasher, tx);
    }

    @Bean
    UpdateUser updateUser(UserAccountRepository users, PasswordHasher hasher, TransactionRunner tx) {
        return new UpdateUser(users, hasher, tx);
    }

    /** First CEO account for an empty database; credentials come from the environment, never from code. */
    @Bean
    CommandLineRunner bootstrapAdmin(UserAccountRepository users, CreateUser createUser,
                                     @Value("${app.bootstrap.admin-username:}") String username,
                                     @Value("${app.bootstrap.admin-password:}") String password) {
        return args -> {
            if (users.count() > 0) return;
            if (username.isBlank() || password.isBlank()) {
                log.warn("No users exist. Set ERP_ADMIN_USERNAME and ERP_ADMIN_PASSWORD to create the first CEO account.");
                return;
            }
            createUser.handle(new CreateUserCommand(username, password, "Administrator", UserRole.CEO));
            log.info("Created initial CEO account '{}'", username);
        };
    }
}
