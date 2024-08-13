package com.project.shopapp.services.Impl;

import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.Productimage;
import com.project.shopapp.responses.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IProductService {
  Product createProduct(ProductDTO productDTO) throws Exception;

  Page<ProductResponse> getAllProducts(PageRequest pageRequest);

  Product getProductById(Long id) throws Exception;

  Product updateProduct(Long id, ProductDTO productDTO) throws Exception;

  void deleteProduct(Long id);

  boolean existsByNameProduct(String name);

  Productimage createProductImage(
      Long productId,
      ProductImageDTO productImageDTO
  ) throws Exception;
}
