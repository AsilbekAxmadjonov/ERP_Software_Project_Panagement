package org.example.erp.modules.identity.application.actions;

import org.example.erp.modules.identity.application.commands.UpdateUserCommand;
import org.example.erp.modules.identity.application.contracts.PasswordHasher;
import org.example.erp.modules.identity.application.dto.UserDTO;
import org.example.erp.modules.identity.application.enums.UserRole;
import org.example.erp.modules.identity.application.events.UserDisabled;
import org.example.erp.modules.identity.application.events.UserEnabled;
import org.example.erp.modules.identity.application.events.UserRoleChanged;
import org.example.erp.modules.identity.application.events.UserUpdated;
import org.example.erp.modules.identity.domain.Role;
import org.example.erp.modules.identity.domain.UserAccount;
import org.example.erp.modules.identity.domain.UserAccountRepository;
import org.example.erp.platform.IntegrationEventPublisher;
import org.example.erp.platform.NotFoundException;
import org.example.erp.platform.TransactionRunner;

public final class UpdateUser {
    private final UserAccountRepository users;
    private final PasswordHasher hasher;
    private final IntegrationEventPublisher events;
    private final TransactionRunner tx;

    public UpdateUser(UserAccountRepository users, PasswordHasher hasher, IntegrationEventPublisher events, TransactionRunner tx) {
        this.users = users;
        this.hasher = hasher;
        this.events = events;
        this.tx = tx;
    }

    public UserDTO handle(UpdateUserCommand c) {
        return tx.run(() -> {
            UserAccount user = users.findById(c.id()).orElseThrow(() -> new NotFoundException("User not found"));
            UserRole previousRole = UserRole.valueOf(user.getRole().name());
            boolean wasActive = user.isActive();
            Role role = c.role() == null ? user.getRole() : UserMapper.toDomain(c.role());
            user.updateProfile(c.fullName() == null ? user.getFullName() : c.fullName(), role);
            if (c.active() != null) {
                if (c.active()) user.activate(); else user.deactivate();
            }
            if (c.password() != null) user.changePasswordHash(hasher.hash(c.password()));
            UserDTO saved = UserMapper.toDto(users.save(user));

            events.publish(new UserUpdated(saved.id(), saved.username(), saved.fullName(), saved.role(), saved.active(),
                    c.password() != null));
            if (saved.role() != previousRole) events.publish(new UserRoleChanged(saved.id(), saved.username(), previousRole, saved.role()));
            if (wasActive && !saved.active()) events.publish(new UserDisabled(saved.id(), saved.username()));
            if (!wasActive && saved.active()) events.publish(new UserEnabled(saved.id(), saved.username()));
            return saved;
        });
    }
}
