package com.Amazon.clone.dto.Product;

import com.Amazon.clone.entity.enums.ProductStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;

    @Size(max = 280, message = "Product slug cannot exceed 280 characters")
    private String slug;

    private String description;

    private UUID brandId;

    private Set<UUID> categoryIds;

    private ProductStatus status;
}
