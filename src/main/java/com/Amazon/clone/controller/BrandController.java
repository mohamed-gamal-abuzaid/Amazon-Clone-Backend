package com.Amazon.clone.controller;

import com.Amazon.clone.dto.Brand.BrandRequest;
import com.Amazon.clone.dto.Brand.BrandResponse;
import com.Amazon.clone.dto.Brand.BrandUpdateRequest;
import com.Amazon.clone.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Tag(name = "Brand", description = "APIs for managing catalog brands")
public class BrandController {

    private final BrandService brandService;


    @GetMapping
    @Operation(summary = "Get all brands", description = "Get list of all available brands (Public)")
    public ResponseEntity<List<BrandResponse>> getAllBrands() {
        return ResponseEntity.ok(brandService.findAllBrands());
    }


    @GetMapping("/{brandId}")
    @Operation(summary = "Get brand by ID", description = "Get details of a brand by its ID (Public)")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable UUID brandId) {
        return ResponseEntity.ok(brandService.findByBrandId(brandId));
    }


    @GetMapping("/name/{brandName}")
    @Operation(summary = "Get brand by name", description = "Get details of a brand by its name (Public)")
    public ResponseEntity<BrandResponse> getBrandByName(@PathVariable String brandName) {
        return ResponseEntity.ok(brandService.findByBrandName(brandName));
    }


        @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new brand", description = "Create a new brand (Admin only)")
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request) {
        BrandResponse response = brandService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    @Operation(summary = "Update a brand", description = "Update an existing brand (Admin only)")
    public ResponseEntity<BrandResponse> updateBrand(@Valid @RequestBody BrandUpdateRequest request) {
        BrandResponse response = brandService.updateBrand(request);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{brandId}")
    @Operation(summary = "Delete a brand", description = "Delete an existing brand (Admin only)")
    public ResponseEntity<Void> deleteBrand(@PathVariable UUID brandId) {
        brandService.deleteBrand(brandId);
        return ResponseEntity.noContent().build();
    }
}
