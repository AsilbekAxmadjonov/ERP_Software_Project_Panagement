package org.example.erp.modules.inventory.infrastructure;

import org.example.erp.modules.inventory.domain.Product;
import org.example.erp.modules.inventory.domain.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaProductRepository implements ProductRepository {
    private final ProductJpaRepository jpa;

    JpaProductRepository(ProductJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Product> findById(long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Product> findAllByName() {
        return jpa.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsByCode(code);
    }

    @Override
    public Product save(Product p) {
        ProductRecord r = p.getId() == null ? new ProductRecord() : jpa.getReferenceById(p.getId());
        r.setCode(p.getCode());
        r.setName(p.getName());
        r.setUnit(p.getUnit());
        r.setSalePrice(p.getSalePrice());
        r.setQuantity(p.getQuantity());
        r.setAverageCost(p.getAverageCost());
        r.setActive(p.isActive());
        return toDomain(jpa.save(r));
    }

    private Product toDomain(ProductRecord r) {
        return Product.restore(r.getId(), r.getCode(), r.getName(), r.getUnit(), r.getSalePrice(), r.getQuantity(),
                r.getAverageCost(), r.isActive());
    }
}
