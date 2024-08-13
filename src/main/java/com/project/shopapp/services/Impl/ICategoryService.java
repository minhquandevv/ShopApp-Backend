package com.project.shopapp.services.Impl;

import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.models.Category;

import java.util.List;

public interface ICategoryService {
    Category createCategory(CategoryDTO categoryDTO);

    Category updateCategory(Long categoryId, CategoryDTO category);

    void deleteCategory(Long id);

    List<Category> getAllCategories();

    Category getCategoryById(Long id);

}
