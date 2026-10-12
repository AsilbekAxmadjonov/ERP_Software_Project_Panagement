package org.example.erp.modules.identity.application.actions;

import org.example.erp.modules.identity.application.commands.CreateUserCommand;
import org.example.erp.modules.identity.application.contracts.PasswordHasher;
import org.example.erp.modules.identity.application.dto.UserDTO;
import org.example.erp.modules.identity.domain.UserAccount;
import org.example.erp.modules.identity.domain.UserAccountRepository;
import org.example.erp.platform.BusinessRuleException;
import org.example.erp.platform.TransactionRunner;

public final class CreateUser {
    private final UserAccountRepository users;
    private final PasswordHasher hasher;
    private final TransactionRunner tx;

    public CreateUser(UserAccountRepository users, PasswordHasher hasher, TransactionRunner tx) {
        this.users = users;
        this.hasher = hasher;
        this.tx = tx;
    }

    public UserDTO handle(CreateUserCommand c) {
        String username = c.username().trim();
        return tx.run(() -> {
            if (users.existsByUsername(username)) throw new BusinessRuleException("Username already exists");
            UserAccount user = UserAccount.register(username, hasher.hash(c.password()), c.fullName(), UserMapper.toDomain(c.role()));
            return UserMapper.toDto(users.save(user));
        });
    }
}
