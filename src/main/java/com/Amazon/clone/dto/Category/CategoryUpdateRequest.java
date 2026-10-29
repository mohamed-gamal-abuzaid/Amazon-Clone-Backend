package com.Amazon.clone.dto.Category;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryUpdateRequest {

    @NotNull(message = "Category ID is required")
    private UUID id;

    private String name;

    private String slug;

    private String description;

    private UUID parentCategoryId;
}
