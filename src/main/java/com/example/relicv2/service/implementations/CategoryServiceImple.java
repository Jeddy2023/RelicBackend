package com.example.relicv2.service.implementations;

import com.example.relicv2.dto.CategoryDto;
import com.example.relicv2.exception.AlreadyExistsException;
import com.example.relicv2.exception.CustomException;
import com.example.relicv2.exception.ResourceNotFoundException;
import com.example.relicv2.model.Category;
import com.example.relicv2.repository.CategoryRepository;
import com.example.relicv2.service.AuditTrialService;
import com.example.relicv2.service.CategoryService;
import com.example.relicv2.service.NotificationService;
import com.example.relicv2.util.UserAuthenticationHelper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImple implements CategoryService {
    private CategoryRepository categoryRepository;
    private ModelMapper modelMapper;
    private final AuditTrialService auditTrialService;
    private final NotificationService notificationService;
    private UserAuthenticationHelper userAuthenticationHelper;

    public CategoryServiceImple(CategoryRepository categoryRepository, ModelMapper modelMapper, AuditTrialService auditTrialService, NotificationService notificationService, UserAuthenticationHelper userAuthenticationHelper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
        this.auditTrialService = auditTrialService;
        this.notificationService = notificationService;
        this.userAuthenticationHelper = userAuthenticationHelper;
    }

    @Override
    public String createCategory(Category category) {
        if (category.getName() == null) {
            throw new CustomException("All fields are required");
        }

        if (categoryRepository.existsByName(category.getName())) {
            throw new AlreadyExistsException("Category already exists");
        }

        categoryRepository.save(category);
        notificationService.sendAdminNotification("New Category has been created: " + category.getName());
        auditTrialService.addToAuditTrial(userAuthenticationHelper.getLoggedInUser(), "Create Category", "Created category: " + category.getName());
        return "Category Created Succesfully";
    }

    @Override
    public List<Category> getAllCategories() {
        List<Category> allCategories = categoryRepository.findAll();
        if (allCategories.isEmpty()) {
            return Collections.emptyList();
        }
        return allCategories;
    }

    @Override
    public List<CategoryDto> getAllCategoriesAdmin(
            String categoryName,
            String sortBy
    ) {
        List<Category> categories = categoryRepository.findAll();

        if (categoryName != null) {
            categories = categories.stream()
                    .filter(category -> category.getName().toLowerCase().contains(categoryName.toLowerCase()))
                    .collect(Collectors.toList());
        }

        switch (sortBy) {
            case "Most Active: High to Low":
                categories.sort(Comparator.comparingInt(category -> category.getProducts().size()));
                Collections.reverse(categories);
                break;
            case "Most Active: Low to High":
                categories.sort(Comparator.comparingInt(category -> category.getProducts().size()));
                break;
            case "Alphabetical Order: A-Z":
                categories.sort(Comparator.comparing(Category::getName));
                break;
            case "Alphabetical Order: Z-A":
                categories.sort(Comparator.comparing(Category::getName).reversed());
                break;
            default:
                throw new IllegalArgumentException("Invalid sorting option: " + sortBy);
        }

        return categories.stream()
                .map(category -> {
                    CategoryDto categoryDto = modelMapper.map(category, CategoryDto.class);
                    categoryDto.setProductCount(category.getProducts().size());
                    return categoryDto;
                })
                .toList();
    }

    @Override
    public String updateCategoryById(Long categoryId, Category category) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        if (optionalCategory.isEmpty()) {
            throw new ResourceNotFoundException("Category not found");
        }
        if (category.getName() != null && category.getName().isEmpty()) {
            throw new CustomException("Category name cannot be empty");
        }
        if (category.getName() != null && categoryRepository.existsByName(category.getName())) {
            throw new AlreadyExistsException("Category already exists");
        }
        Category updatedCategory = optionalCategory.get();
        updatedCategory.setName(category.getName());
        categoryRepository.save(updatedCategory);
        notificationService.sendAdminNotification("Category has been updated: " + optionalCategory.get().getName());
        auditTrialService.addToAuditTrial(userAuthenticationHelper.getLoggedInUser(), "Update Category", "Updated category: " + updatedCategory.getName());
        return "Category Updated succesfully";
    }

    @Override
    public String deleteCategoryById(Long categoryId) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        if (optionalCategory.isEmpty()) {
            throw new ResourceNotFoundException("Category not found");
        }
        if(optionalCategory.get().getProducts().isEmpty()){
            throw new CustomException("Cannot delete category with associated products");
        }
        categoryRepository.delete(optionalCategory.get());
        notificationService.sendAdminNotification("Category has been deleted: " + optionalCategory.get().getName());
        auditTrialService.addToAuditTrial(userAuthenticationHelper.getLoggedInUser(), "Delete Category", "Deleted category: " + optionalCategory.get().getName());
        return "Category Deleted Succesfully";
    }
    @Override
    public List<Category> getTopFiveActiveCategories() {
        return categoryRepository.findTopFiveActiveCategories();
    }
}
