package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.product.CategoryRequest;
import com.ask.dto.request.product.ProductRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.product.ProductCategoryResponse;
import com.ask.dto.response.product.ProductResponse;
import com.ask.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing products catalogue and product categories.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ==================== PRODUCT CATEGORIES ====================

    @PostMapping(ApiPaths.PRODUCT_CATEGORIES)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> createCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CategoryRequest request) {
        ProductCategoryResponse response = productService.createCategory(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Category created successfully", ApiPaths.PRODUCT_CATEGORIES));
    }

    @GetMapping(ApiPaths.PRODUCT_CATEGORIES)
    public ResponseEntity<ApiResponse<List<ProductCategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllCategories(), ApiPaths.PRODUCT_CATEGORIES));
    }

    @PatchMapping(ApiPaths.PRODUCT_CATEGORIES + "/{id}/toggle")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        productService.toggleCategoryStatus(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Category status updated", ApiPaths.PRODUCT_CATEGORIES + "/" + id + "/toggle"));
    }

    // ==================== PRODUCTS ====================

    @PostMapping(ApiPaths.PRODUCTS)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN') or hasAuthority('PERM_INVENTORY_ADD_STOCK')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Product added to catalogue successfully", ApiPaths.PRODUCTS));
    }

    @GetMapping(ApiPaths.PRODUCTS)
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ProductResponse> response = productService.getProducts(search, categoryId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.PRODUCTS));
    }

    @GetMapping(ApiPaths.PRODUCTS + "/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id), ApiPaths.PRODUCTS + "/" + id));
    }

    @PutMapping(ApiPaths.PRODUCTS + "/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Product updated in catalogue", ApiPaths.PRODUCTS + "/" + id));
    }

    @PatchMapping(ApiPaths.PRODUCTS + "/{id}/toggle")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        productService.toggleProductStatus(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Product status updated", ApiPaths.PRODUCTS + "/" + id + "/toggle"));
    }
}
