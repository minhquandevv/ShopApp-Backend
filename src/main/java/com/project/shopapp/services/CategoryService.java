package com.project.shopapp.services;

import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.models.Category;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.services.Impl.ICategoryService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {
  private final CategoryRepository categoryRepository;

  @Override
  @Transactional
  public Category createCategory(CategoryDTO categoryDTO) {
    Category newCategory = Category
        .builder()
        .name(categoryDTO.getName())
        .build();
    return categoryRepository.save(newCategory);
  }

  @Override
  public Category getCategoryById(Long id) {
    return categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Category not found"));
  }

  @Override
  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  @Override
  @Transactional

  public Category updateCategory(Long categoryId, CategoryDTO categoryDTO) {
    Category existingCategory = getCategoryById(categoryId);
    existingCategory.setName(categoryDTO.getName());
    categoryRepository.save(existingCategory);
    return existingCategory;
  }


  @Override
  @Transactional

  public void deleteCategory(Long id) {
    categoryRepository.deleteById(id);
  }
}
