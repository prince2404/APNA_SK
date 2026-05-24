package com.ask.service;

import com.ask.dto.request.product.CategoryRequest;
import com.ask.dto.request.product.ProductRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.product.ProductCategoryResponse;
import com.ask.dto.response.product.ProductResponse;

import java.util.List;

/**
 * Service interface for managing product categories and product catalogue.
 */
public interface ProductService {

    // Categories
    ProductCategoryResponse createCategory(CategoryRequest request, String currentUserEmail);
    List<ProductCategoryResponse> getAllCategories();
    void toggleCategoryStatus(Long id, String currentUserEmail);

    // Products
    ProductResponse createProduct(ProductRequest request, String currentUserEmail);
    ProductResponse updateProduct(Long id, ProductRequest request, String currentUserEmail);
    ProductResponse getProductById(Long id);
    PageResponse<ProductResponse> getProducts(String search, Long categoryId, int page, int size);
    void toggleProductStatus(Long id, String currentUserEmail);
}
