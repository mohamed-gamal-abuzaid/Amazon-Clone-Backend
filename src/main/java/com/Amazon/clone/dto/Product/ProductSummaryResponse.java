package com.Amazon.clone.dto.Product;

import com.Amazon.clone.entity.enums.ProductStatus;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryResponse {

    private UUID id;
    private String name;
    private String slug;
    private UUID brandId;
    private String brandName;
    private ProductStatus status;
    private String mainImageUrl;
    private int variantsCount;
    private OffsetDateTime createdAt;
}
