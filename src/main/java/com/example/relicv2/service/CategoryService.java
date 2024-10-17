package com.example.relicv2.service;

import com.example.relicv2.dto.CategoryDto;
import com.example.relicv2.model.Category;

import java.util.List;

public interface CategoryService {
    String createCategory(Category category);
    List<Category> getAllCategories();
    List<CategoryDto> getAllCategoriesAdmin(
            String categoryName,
            String sortBy
    );
    String updateCategoryById(Long categoryId, Category category);
    String deleteCategoryById(Long categoryId);
    List<Category> getTopFiveActiveCategories();
}
