package org.example.erp.modules.accounting.infrastructure;

import org.example.erp.modules.accounting.domain.Account;
import org.example.erp.modules.accounting.domain.AccountRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaAccountRepository implements AccountRepository {
    private final AccountJpaRepository jpa;

    JpaAccountRepository(AccountJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Account> findByCode(String code) {
        return jpa.findByCode(code).map(this::toDomain);
    }

    @Override
    public List<Account> findAllByCode() {
        return jpa.findAllByOrderByCodeAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public Account save(Account a) {
        AccountRecord r = a.getId() == null ? new AccountRecord() : jpa.getReferenceById(a.getId());
        r.setCode(a.getCode());
        r.setName(a.getName());
        r.setType(a.getType());
        r.setActive(a.isActive());
        return toDomain(jpa.save(r));
    }

    private Account toDomain(AccountRecord r) {
        return Account.restore(r.getId(), r.getCode(), r.getName(), r.getType(), r.isActive());
    }
}
