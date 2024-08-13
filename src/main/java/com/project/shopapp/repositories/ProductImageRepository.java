package com.project.shopapp.repositories;

import com.project.shopapp.models.Product;
import com.project.shopapp.models.Productimage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<Productimage, Long> {
  List<Productimage> findByProductId(Long productId);
}
