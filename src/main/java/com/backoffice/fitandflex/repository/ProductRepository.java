package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
