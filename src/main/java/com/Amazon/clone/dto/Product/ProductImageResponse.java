package com.Amazon.clone.dto.Product;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {

    private UUID id;
    private String imageUrl;
    private String altText;
    private int sortOrder;
    private OffsetDateTime createdAt;
}
