package com.project.shopapp.services;

import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.InvalidParamException;
import com.project.shopapp.models.Category;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.Productimage;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.repositories.ProductImageRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.services.Impl.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductImageRepository productImageRepository;

  @Override
  public Product createProduct(ProductDTO productDTO) throws DataNotFoundException {
    Category existingCategory = categoryRepository
        .findById(productDTO.getCategoryId())
        .orElseThrow(
            () -> new DataNotFoundException("Cannot find category with id: " + productDTO.getCategoryId()));
    Product newProduct = Product.builder()
        .name(productDTO.getName())
        .price(productDTO.getPrice())
        .thumbnail(productDTO.getThumbnail())
        .description(productDTO.getDescription())
        .category(existingCategory)
        .build();
    return productRepository.save(newProduct);
  }

  @Override
  public Product getProductById(Long productId) throws Exception {
    return productRepository.findById(productId)
        .orElseThrow(() -> new DataNotFoundException("Cannot find product with id: " + productId));
  }

  @Override
  public Page<ProductResponse> getAllProducts(PageRequest pageRequest) {
    return productRepository.findAll(pageRequest).map(ProductResponse::fromProduct);
  }

  @Override
  public Product updateProduct(Long id, ProductDTO productDTO) throws Exception {
    Product existingProduct = getProductById(id);
    if (existingProduct != null) {
      Category existingCategory = categoryRepository
          .findById(productDTO.getCategoryId())
          .orElseThrow(
              () -> new DataNotFoundException("Cannot find category with id: " + productDTO.getCategoryId()));
      existingProduct.setName(productDTO.getName());
      existingProduct.setCategory(existingCategory);
      existingProduct.setPrice(productDTO.getPrice());
      existingProduct.setThumbnail(productDTO.getThumbnail());
      existingProduct.setDescription(productDTO.getDescription());
      return productRepository.save(existingProduct);
    }
    return null;
  }

  @Override
  public void deleteProduct(Long id) {
    Optional<Product> productOptional = productRepository.findById(id);
    productOptional.ifPresent(productRepository::delete);
  }

  @Override
  public boolean existsByNameProduct(String name) {
    return productRepository.existsByName(name);
  }

  @Override
  //Product Image
  public Productimage createProductImage(
      Long productId,
      ProductImageDTO productImageDTO
  ) throws Exception {
    Product existingProduct = productRepository
        .findById(productId)
        .orElseThrow(
            () -> new DataNotFoundException("Cannot find category with id: " + productImageDTO.getProductId()));
    Productimage newProductImage = Productimage
        .builder()
        .product(existingProduct)
        .imageUrl(productImageDTO.getImageUrl())
        .build();
    //Khong cho insert qua 5 anh cho 1 sp
    int size = productImageRepository.findByProductId(productId).size();
    if (size >= Productimage.MAXIMUM_IMAGES_PER_PRODUCT) {
      throw new InvalidParamException("Number if image must be less than 5!");
    }
    return productImageRepository.save(newProductImage);
  }
}
