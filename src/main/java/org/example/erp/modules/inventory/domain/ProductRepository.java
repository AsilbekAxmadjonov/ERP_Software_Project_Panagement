package org.example.erp.modules.inventory.domain;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(long id);

    List<Product> findAllByName();

    boolean existsByCode(String code);

    Product save(Product product);
}
