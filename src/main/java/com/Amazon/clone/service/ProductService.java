package com.Amazon.clone.service;

import com.Amazon.clone.dto.Brand.BrandResponse;
import com.Amazon.clone.dto.Category.CategoryResponse;
import com.Amazon.clone.dto.Product.*;
import com.Amazon.clone.entity.*;
import com.Amazon.clone.entity.enums.ProductStatus;
import com.Amazon.clone.exception.ResourceNotFoundException;
import com.Amazon.clone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::buildProductSummaryResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return buildProductResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return buildProductResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Product with the same name already exists: " + request.getName());
        }
        if (productRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Product with the same slug already exists: " + request.getSlug());
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setDescription(request.getDescription());
        product.setStatus(request.getStatus() != null ? request.getStatus() : ProductStatus.DRAFT);

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));
            product.setBrand(brand);
        }

        product = productRepository.save(product);

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            for (Category category : categories) {
                ProductCategory pc = new ProductCategory();
                pc.setProduct(product);
                pc.setCategory(category);
                pc.getId().setProductId(product.getId());
                pc.getId().setCategoryId(category.getId());
                product.getProductCategories().add(pc);
            }
            product = productRepository.save(product);
        }

        return buildProductResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            if (!product.getName().equals(request.getName()) && productRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Product with the same name already exists: " + request.getName());
            }
            product.setName(request.getName());
        }

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            if (!product.getSlug().equals(request.getSlug()) && productRepository.existsBySlug(request.getSlug())) {
                throw new IllegalArgumentException("Product with the same slug already exists: " + request.getSlug());
            }
            product.setSlug(request.getSlug());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));
            product.setBrand(brand);
        }

        if (request.getCategoryIds() != null) {
            product.getProductCategories().clear();
            if (!request.getCategoryIds().isEmpty()) {
                List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
                for (Category category : categories) {
                    ProductCategory pc = new ProductCategory();
                    pc.setProduct(product);
                    pc.setCategory(category);
                    pc.getId().setProductId(product.getId());
                    pc.getId().setCategoryId(category.getId());
                    product.getProductCategories().add(pc);
                }
            }
        }

        product = productRepository.save(product);
        return buildProductResponse(product);
    }

    @Transactional
    public VariantResponse addVariant(UUID productId, CreateVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Variant with SKU already exists: " + request.getSku());
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(request.getSku());
        variant.setAttributes(request.getAttributes() != null ? request.getAttributes() : new LinkedHashMap<>());

        ProductVariant savedVariant = productVariantRepository.save(variant);
        return buildVariantResponse(savedVariant);
    }

    @Transactional
    public ProductImageResponse addImage(UUID productId, CreateProductImageRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(request.getImageUrl());
        image.setAltText(request.getAltText());
        image.setSortOrder(request.getSortOrder());

        ProductImage savedImage = productImageRepository.save(image);
        return buildProductImageResponse(savedImage);
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        productRepository.delete(product);
    }

    private ProductSummaryResponse buildProductSummaryResponse(Product product) {
        String mainImageUrl = product.getImages() != null ? product.getImages().stream()
                .min(Comparator.comparingInt(ProductImage::getSortOrder))
                .map(ProductImage::getImageUrl)
                .orElse(null) : null;

        int variantsCount = product.getVariants() != null ? product.getVariants().size() : 0;

        return ProductSummaryResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .status(product.getStatus())
                .mainImageUrl(mainImageUrl)
                .variantsCount(variantsCount)
                .createdAt(product.getCreatedAt())
                .build();
    }

    private ProductResponse buildProductResponse(Product product) {
        BrandResponse brandResp = null;
        if (product.getBrand() != null) {
            brandResp = BrandResponse.builder()
                    .id(product.getBrand().getId())
                    .name(product.getBrand().getName())
                    .slug(product.getBrand().getSlug())
                    .build();
        }

        List<CategoryResponse> categories = null;
        if (product.getProductCategories() != null) {
            categories = product.getProductCategories().stream()
                    .map(pc -> CategoryResponse.builder()
                            .id(pc.getCategory().getId())
                            .name(pc.getCategory().getName())
                            .slug(pc.getCategory().getSlug())
                            .description(pc.getCategory().getDescription())
                            .build())
                    .toList();
        }

        List<ProductImageResponse> images = null;
        if (product.getImages() != null) {
            images = product.getImages().stream()
                    .sorted(Comparator.comparingInt(ProductImage::getSortOrder))
                    .map(this::buildProductImageResponse)
                    .toList();
        }

        List<VariantResponse> variants = null;
        if (product.getVariants() != null) {
            variants = product.getVariants().stream()
                    .map(this::buildVariantResponse)
                    .toList();
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .status(product.getStatus())
                .brand(brandResp)
                .categories(categories)
                .images(images)
                .variants(variants)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private VariantResponse buildVariantResponse(ProductVariant variant) {
        return VariantResponse.builder()
                .id(variant.getId())
                .productId(variant.getProduct() != null ? variant.getProduct().getId() : null)
                .sku(variant.getSku())
                .attributes(variant.getAttributes())
                .createdAt(variant.getCreatedAt())
                .build();
    }

    private ProductImageResponse buildProductImageResponse(ProductImage image) {
        return ProductImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .altText(image.getAltText())
                .sortOrder(image.getSortOrder())
                .createdAt(image.getCreatedAt())
                .build();
    }
}
