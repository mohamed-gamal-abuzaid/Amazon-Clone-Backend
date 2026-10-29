package com.Amazon.clone.service;

import com.Amazon.clone.dto.Brand.BrandRequest;
import com.Amazon.clone.dto.Brand.BrandResponse;
import com.Amazon.clone.dto.Brand.BrandUpdateRequest;
import com.Amazon.clone.entity.Brand;
import com.Amazon.clone.exception.ResourceNotFoundException;
import com.Amazon.clone.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public BrandResponse findByBrandId(UUID brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + brandId));
        return buildBrandResponse(brand);
    }

    @Transactional(readOnly = true)
    public BrandResponse findByBrandName(String brandName) {
        Brand brand = brandRepository.findByName(brandName)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with name: " + brandName));
        return buildBrandResponse(brand);
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> findAllBrands() {
        return brandRepository.findAll()
                .stream()
                .map(this::buildBrandResponse)
                .toList();
    }

    @Transactional
    public BrandResponse createBrand(BrandRequest brandRequest) {
        Brand brand = new Brand();
        brand.setName(brandRequest.getName());
        brand.setSlug(brandRequest.getSlug());
        brandRepository.save(brand);

        return buildBrandResponse(brand);
    }

    @Transactional
    public BrandResponse updateBrand(BrandUpdateRequest brandRequest) {
        Brand brand = brandRepository.findById(brandRequest.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + brandRequest.getId()));

        if (brandRequest.getSlug() != null && !brandRequest.getSlug().isBlank()) {
            brand.setSlug(brandRequest.getSlug());
        }
        if (brandRequest.getName() != null && !brandRequest.getName().isBlank()) {
            brand.setName(brandRequest.getName());
        }

        brandRepository.save(brand);
        return buildBrandResponse(brand);
    }

    @Transactional
    public void deleteBrand(UUID brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + brandId));
        brandRepository.delete(brand);
    }

    private BrandResponse buildBrandResponse(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .build();
    }
}
