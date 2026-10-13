package org.example.erp.modules.accounting.domain;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findByCode(String code);

    List<Account> findAllByCode();

    long count();

    Account save(Account account);
}
