package org.example.erp.modules.identity.application.actions;

import org.example.erp.modules.identity.application.commands.UpdateUserCommand;
import org.example.erp.modules.identity.application.contracts.PasswordHasher;
import org.example.erp.modules.identity.application.dto.UserDTO;
import org.example.erp.modules.identity.domain.Role;
import org.example.erp.modules.identity.domain.UserAccount;
import org.example.erp.modules.identity.domain.UserAccountRepository;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

public final class UpdateUser {
    private final UserAccountRepository users;
    private final PasswordHasher hasher;
    private final TransactionRunner tx;

    public UpdateUser(UserAccountRepository users, PasswordHasher hasher, TransactionRunner tx) {
        this.users = users;
        this.hasher = hasher;
        this.tx = tx;
    }

    public UserDTO handle(UpdateUserCommand c) {
        return tx.run(() -> {
            UserAccount user = users.findById(c.id()).orElseThrow(() -> new NotFoundException("User not found"));
            Role role = c.role() == null ? user.getRole() : UserMapper.toDomain(c.role());
            user.updateProfile(c.fullName() == null ? user.getFullName() : c.fullName(), role);
            if (c.active() != null) {
                if (c.active()) user.activate(); else user.deactivate();
            }
            if (c.password() != null) user.changePasswordHash(hasher.hash(c.password()));
            return UserMapper.toDto(users.save(user));
        });
    }
}
