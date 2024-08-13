package com.project.shopapp.controllers;

import com.github.javafaker.Faker;
import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.Productimage;
import com.project.shopapp.responses.ProductListResponse;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;

  @GetMapping("")
  public ResponseEntity<ProductListResponse> getAllProducts(
      @RequestParam("page") int page,
      @RequestParam("limit") int limit
  ) {
    PageRequest pageRequest = PageRequest.of(page, limit,
        Sort.by("createdAt").descending());
    Page<ProductResponse> products = productService.getAllProducts(pageRequest);
    int totalPages = products.getTotalPages();
    List<ProductResponse> productList = products.getContent();
    return ResponseEntity.ok(ProductListResponse
        .builder()
        .products(productList)
        .totalPages(totalPages)
        .build());
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getProductById(@PathVariable("id") Long id) {
    try {
      Product existingProduct = productService.getProductById(id);
      return ResponseEntity.ok(ProductResponse.fromProduct(existingProduct));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("")
  public ResponseEntity<?> createProducts(
      @Valid
      @RequestBody
      ProductDTO productDTO,
      BindingResult result) {
    try {
      if (result.hasErrors()) {
        List<String> errorMessage = result.getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .toList();
        return ResponseEntity.badRequest().body(errorMessage);
      }
      Product newProduct = productService.createProduct(productDTO);
      return ResponseEntity.ok().body(newProduct);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  //Request upload image
  @PostMapping(value = "uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> uploadImage(@PathVariable("id") Long productId, @ModelAttribute("files") List<MultipartFile> files
  ) {
    try {
      Product existingProduct = productService.getProductById(productId);
      files = files == null ? new ArrayList<MultipartFile>() : files;
      if (files.size() > Productimage.MAXIMUM_IMAGES_PER_PRODUCT) {
        return ResponseEntity.badRequest().body("You can only upload more than 5 images");
      }
      List<Productimage> productimages = new ArrayList<>();
      for (MultipartFile file : files) {
        if (file.getSize() == 0) {
          continue;
        }
        if (file.getSize() > 10 * 1024 * 1024) {
          return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
              .body("File is too large! Maximum size is 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
          return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
              .body("Unsupported content type: " + contentType);
        }
        String filename = storeFile(file);
        //Luu vao doi tuong trong Product
        Productimage productimage = productService.createProductImage(existingProduct
                .getId(),
            ProductImageDTO.builder()
                .imageUrl(filename)
                .build()
        );
        productimages.add(productimage);
      }
      return ResponseEntity.ok().body(productimages);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }


  private String storeFile(MultipartFile file) throws IOException {
    if (file.getOriginalFilename() == null) {
      throw new IOException("Filename cannot be empty");
    }
    String filename = StringUtils.cleanPath(file.getOriginalFilename());
    //add UUID
    String uniqueFileName = UUID.randomUUID().toString() + "." + filename;
    Path uploadDir = Paths.get("uploads");
    //Check ton tai cua thu muc
    if (!Files.exists(uploadDir)) {
      Files.createDirectory(uploadDir);
    }
    //Get full path file
    Path destination = Paths.get(uploadDir.toString(), uniqueFileName);
    //copy file to dir
    Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
    return uniqueFileName;
  }

  @PutMapping("/{id}")
  public ResponseEntity<String> updateProducts(@PathVariable Long id) {
    return ResponseEntity.status(HttpStatus.CREATED).body(String.format("Update product by id: %s", id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteProducts(@PathVariable Long id) {
    try {
      productService.deleteProduct(id);
      return ResponseEntity.status(HttpStatus.OK).body(String.format("Delete product by id: %s", id));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

//  @PostMapping("/generateFakeProducts")
  private ResponseEntity<String> generateFakeProducts() {
    Faker faker = new Faker();
    for (int i = 0; i < 1_000_000; i++) {
      String productName = faker.commerce().productName();
      if (productService.existsByNameProduct(productName)) {
        continue;
      }
      ProductDTO productDTO = ProductDTO
          .builder()
          .name(productName)
          .price((float) faker.number().numberBetween(10, 90_000_000))
          .description(faker.lorem().sentence())
          .categoryId((long) faker.number().numberBetween(1, 3))
          .build();
      try {
        productService.createProduct(productDTO);
      } catch (DataNotFoundException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
      }
    }
    return ResponseEntity.ok("Fake data successfully!");
  }
}
