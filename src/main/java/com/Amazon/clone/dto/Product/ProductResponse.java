package com.Amazon.clone.dto.Product;

import com.Amazon.clone.dto.Brand.BrandResponse;
import com.Amazon.clone.dto.Category.CategoryResponse;
import com.Amazon.clone.entity.enums.ProductStatus;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private ProductStatus status;
    private BrandResponse brand;
    private List<CategoryResponse> categories;
    private List<ProductImageResponse> images;
    private List<VariantResponse> variants;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
