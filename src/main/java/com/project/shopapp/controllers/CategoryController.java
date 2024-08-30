package com.project.shopapp.controllers;

import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.models.Category;
import com.project.shopapp.responses.CategoryResponse;
import com.project.shopapp.responses.UpdateCategoryResponse;
import com.project.shopapp.services.CategoryService;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {
  private final CategoryService categoryService;
  private final LocalizationUtils localizationUtils;

  @PostMapping("")
  public ResponseEntity<CategoryResponse> createCategory(
      @Valid
      @RequestBody
      CategoryDTO categoryDTO,
      BindingResult result
  ) {
    CategoryResponse categoryResponse = new CategoryResponse();
    if (result.hasErrors()) {
      List<String> errorMessage = result.getFieldErrors()
          .stream()
          .map(FieldError::getDefaultMessage)
          .toList();
      categoryResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_CATEGORY_FAILED));
      categoryResponse.setErrors(errorMessage);
      return ResponseEntity.badRequest().body(categoryResponse);
    }
    Category category = categoryService.createCategory(categoryDTO);
    categoryResponse.setCategory(category);
    return ResponseEntity.ok(categoryResponse);
  }

  @GetMapping("")
  public ResponseEntity<List<Category>> getAllCategories(
      @RequestParam("page") int page,
      @RequestParam("limit") int limit
  ) {
    List<Category> categories = categoryService.getAllCategories();
    return ResponseEntity.ok(categories);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UpdateCategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
    categoryService.updateCategory(id, categoryDTO);
    return ResponseEntity.ok(UpdateCategoryResponse
        .builder()
        .message(localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY))
        .build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.ok(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_CATEGORY_SUCCESSFULLY));
  }
}
