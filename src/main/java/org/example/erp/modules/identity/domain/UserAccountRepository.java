package org.example.erp.modules.identity.domain;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository {
    Optional<UserAccount> findById(long id);

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    List<UserAccount> findAll();

    long count();

    UserAccount save(UserAccount user);
}
