package com.Amazon.clone.dto.Product;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantResponse {

    private UUID id;
    private UUID productId;
    private String sku;
    private Map<String, Object> attributes;
    private OffsetDateTime createdAt;
}
