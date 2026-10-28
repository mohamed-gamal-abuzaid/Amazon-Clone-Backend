package com.Amazon.clone.dto.Brand;

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
public class BrandUpdateRequest {
    @NotNull(message = "Brand ID is required")
    private UUID id;

    private String name;

    private String slug;
}
