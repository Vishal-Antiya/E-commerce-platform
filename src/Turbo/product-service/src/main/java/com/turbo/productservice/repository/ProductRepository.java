package com.turbo.productservice.repository;

import com.turbo.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    //  Add custom query methods if needed
}
            