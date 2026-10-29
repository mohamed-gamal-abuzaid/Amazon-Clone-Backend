package com.Amazon.clone.service;

import com.Amazon.clone.dto.Category.CategoryRequest;
import com.Amazon.clone.dto.Category.CategoryResponse;
import com.Amazon.clone.dto.Category.CategoryUpdateRequest;
import com.Amazon.clone.entity.Category;
import com.Amazon.clone.exception.ResourceNotFoundException;
import com.Amazon.clone.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            throw new IllegalArgumentException("Category with the same name already exists");
        }
        if (categoryRepository.existsBySlug(categoryRequest.getSlug())) {
            throw new IllegalArgumentException("Category with the same slug already exists");
        }

        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setSlug(categoryRequest.getSlug());
        category.setDescription(categoryRequest.getDescription());

        if (categoryRequest.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(categoryRequest.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + categoryRequest.getParentCategoryId()));
            category.setParent(parentCategory);
        }

        Category savedCategory = categoryRepository.save(category);
        return buildCategoryResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(CategoryUpdateRequest categoryRequest) {
        Category category = categoryRepository.findById(categoryRequest.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryRequest.getId()));

        if (categoryRequest.getName() != null && !categoryRequest.getName().isBlank()) {
            if (!category.getName().equals(categoryRequest.getName()) && categoryRepository.existsByName(categoryRequest.getName())) {
                throw new IllegalArgumentException("Category with the same name already exists");
            }
            category.setName(categoryRequest.getName());
        }

        if (categoryRequest.getSlug() != null && !categoryRequest.getSlug().isBlank()) {
            if (!category.getSlug().equals(categoryRequest.getSlug()) && categoryRepository.existsBySlug(categoryRequest.getSlug())) {
                throw new IllegalArgumentException("Category with the same slug already exists");
            }
            category.setSlug(categoryRequest.getSlug());
        }

        if (categoryRequest.getDescription() != null) {
            category.setDescription(categoryRequest.getDescription());
        }

        if (categoryRequest.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(categoryRequest.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + categoryRequest.getParentCategoryId()));
            category.setParent(parentCategory);
        }

        Category updatedCategory = categoryRepository.save(category);
        return buildCategoryResponse(updatedCategory);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Unlink parent from children before deleting
        if (category.getChildren() != null) {
            category.getChildren().forEach(child -> child.setParent(null));
        }

        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        return buildCategoryResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::buildCategoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<CategoryResponse> getAllChildrenOfCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        return category.getChildren() != null
                ? category.getChildren().stream().map(this::buildCategoryResponse).collect(Collectors.toSet())
                : Set.of();
    }

    private CategoryResponse buildCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentCategoryId(category.getParent() != null ? category.getParent().getId() : null)
                .childCategories(category.getChildren() != null ? category.getChildren().stream()
                        .map(child -> CategoryResponse.builder()
                                .id(child.getId())
                                .name(child.getName())
                                .slug(child.getSlug())
                                .description(child.getDescription())
                                .parentCategoryId(category.getId())
                                .build())
                        .collect(Collectors.toSet()) : Set.of())
                .build();
    }
}
