package com.project.shopapp.controllers;

import com.github.javafaker.Faker;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.Productimage;
import com.project.shopapp.responses.ProductListResponse;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.services.ProductService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
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
  private final LocalizationUtils localizationUtils;

  @GetMapping("")
  public ResponseEntity<ProductListResponse> getAllProducts(
      @RequestParam("page") int page,
      @RequestParam("limit") int limit
  ) {
    PageRequest pageRequest = PageRequest.of(page, limit,
//        Sort.by("createdAt").descending());
          Sort.by("id").ascending());
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
  @PostMapping(value = "uploads/{id}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  //POST http://localhost:8088/v1/api/products
  public ResponseEntity<?> uploadImages(
      @PathVariable("id") Long productId,
      @ModelAttribute("files") List<MultipartFile> files
  ){
    try {
      Product existingProduct = productService.getProductById(productId);
      files = files == null ? new ArrayList<MultipartFile>() : files;
      if(files.size() > Productimage.MAXIMUM_IMAGES_PER_PRODUCT) {
        return ResponseEntity.badRequest().body(localizationUtils
            .getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_MAX_5));
      }
      List<Productimage> productImages = new ArrayList<>();
      for (MultipartFile file : files) {
        if(file.getSize() == 0) {
          continue;
        }
        // Kiểm tra kích thước file và định dạng
        if(file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
          return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
              .body(localizationUtils
                  .getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE));
        }
        String contentType = file.getContentType();
        if(contentType == null || !contentType.startsWith("image/")) {
          return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
              .body(localizationUtils.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_MUST_BE_IMAGE));
        }
        // Lưu file và cập nhật thumbnail trong DTO
        String filename = storeFile(file); // Thay thế hàm này với code của bạn để lưu file
        //lưu vào đối tượng product trong DB
        Productimage productImage = productService.createProductImage(
            existingProduct.getId(),
            ProductImageDTO.builder()
                .imageUrl(filename)
                .build()
        );
        productImages.add(productImage);
      }
      return ResponseEntity.ok().body(productImages);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
  @GetMapping("/images/{imageName}")
  public ResponseEntity<?> viewImage(@PathVariable String imageName) {
    try {
      java.nio.file.Path imagePath = Paths.get("uploads/"+imageName);
      UrlResource resource = new UrlResource(imagePath.toUri());

      if (resource.exists()) {
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(resource);
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }
  private String storeFile(MultipartFile file) throws IOException {
    if (!isImageFile(file) || file.getOriginalFilename() == null) {
      throw new IOException("Invalid image format");
    }
    String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
    // Thêm UUID vào trước tên file để đảm bảo tên file là duy nhất
    String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
    // Đường dẫn đến thư mục mà bạn muốn lưu file
    java.nio.file.Path uploadDir = Paths.get("uploads");
    // Kiểm tra và tạo thư mục nếu nó không tồn tại
    if (!Files.exists(uploadDir)) {
      Files.createDirectories(uploadDir);
    }
    // Đường dẫn đầy đủ đến file
    java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
    // Sao chép file vào thư mục đích
    Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
    return uniqueFilename;
  }
  private boolean isImageFile(MultipartFile file) {
    String contentType = file.getContentType();
    return contentType != null && contentType.startsWith("image/");
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
