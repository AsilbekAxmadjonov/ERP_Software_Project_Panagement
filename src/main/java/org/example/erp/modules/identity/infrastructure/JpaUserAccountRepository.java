package org.example.erp.modules.identity.infrastructure;

import org.example.erp.modules.identity.domain.UserAccount;
import org.example.erp.modules.identity.domain.UserAccountRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaUserAccountRepository implements UserAccountRepository {
    private final UserJpaRepository jpa;

    JpaUserAccountRepository(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<UserAccount> findById(long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return jpa.findByUsername(username).map(this::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsername(username);
    }

    @Override
    public List<UserAccount> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public UserAccount save(UserAccount user) {
        UserRecord r = user.getId() == null ? new UserRecord() : jpa.getReferenceById(user.getId());
        r.setUsername(user.getUsername());
        r.setPasswordHash(user.getPasswordHash());
        r.setFullName(user.getFullName());
        r.setRole(user.getRole());
        r.setActive(user.isActive());
        return toDomain(jpa.save(r));
    }

    private UserAccount toDomain(UserRecord r) {
        return UserAccount.restore(r.getId(), r.getUsername(), r.getPasswordHash(), r.getFullName(), r.getRole(), r.isActive());
    }
}
