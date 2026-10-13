package org.example.erp.modules.partners.domain;

import java.util.List;
import java.util.Optional;

public interface PartnerRepository {
    Optional<Partner> findById(long id);

    List<Partner> findAllByName();

    Partner save(Partner partner);
}
