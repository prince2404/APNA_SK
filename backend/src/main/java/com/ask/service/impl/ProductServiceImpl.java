package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.request.product.CategoryRequest;
import com.ask.dto.request.product.ProductRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.product.ProductCategoryResponse;
import com.ask.dto.response.product.ProductResponse;
import com.ask.entity.Product;
import com.ask.entity.ProductCategory;
import com.ask.entity.User;
import com.ask.enums.EntityStatus;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.ask.mapper.ProductMapper;
import com.ask.repository.ProductCategoryRepository;
import com.ask.repository.ProductRepository;
import com.ask.repository.UserRepository;
import com.ask.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ProductService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    private void ensureAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        if (!user.getRole().getName().equals(RoleConstants.SUPER_ADMIN)
                && !user.getRole().getName().equals(RoleConstants.SYSTEM_ADMIN)) {
            throw new AccessDeniedException("Access denied. Admin role required.");
        }
    }

    private void validatePricing(BigDecimal mrp, BigDecimal askPrice) {
        if (mrp.compareTo(BigDecimal.ZERO) <= 0 || askPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("MRP and ASK selling price must be greater than 0");
        }
        if (askPrice.compareTo(mrp) > 0) {
            throw new BusinessRuleException("ASK price cannot be greater than MRP");
        }
    }

    @Override
    @Transactional
    public ProductCategoryResponse createCategory(CategoryRequest request, String currentUserEmail) {
        ensureAdmin(currentUserEmail);
        
        categoryRepository.findByName(request.getName()).ifPresent(c -> {
            throw new BusinessRuleException("Category with name '" + request.getName() + "' already exists");
        });

        ProductCategory category = ProductCategory.builder()
                .name(request.getName())
                .status(EntityStatus.ACTIVE)
                .build();
        ProductCategory saved = categoryRepository.save(category);
        return productMapper.toCategoryResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(productMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleCategoryStatus(Long id, String currentUserEmail) {
        ensureAdmin(currentUserEmail);
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product category", "id", id));
        category.setStatus(category.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request, String currentUserEmail) {
        ensureAdmin(currentUserEmail);
        validatePricing(request.getMrp(), request.getAskPrice());

        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Product category", "id", request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .brand(request.getBrand())
                .category(category)
                .hsnCode(request.getHsnCode())
                .mrp(request.getMrp())
                .askPrice(request.getAskPrice())
                .gstPercentage(request.getGstPercentage())
                .minStockThreshold(request.getMinStockThreshold())
                .status(EntityStatus.ACTIVE)
                .build();

        Product saved = productRepository.save(product);
        return productMapper.toProductResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, String currentUserEmail) {
        ensureAdmin(currentUserEmail);
        validatePricing(request.getMrp(), request.getAskPrice());

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Product category", "id", request.getCategoryId()));

        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setCategory(category);
        product.setHsnCode(request.getHsnCode());
        product.setMrp(request.getMrp());
        product.setAskPrice(request.getAskPrice());
        product.setGstPercentage(request.getGstPercentage());
        product.setMinStockThreshold(request.getMinStockThreshold());

        Product saved = productRepository.save(product);
        return productMapper.toProductResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return productMapper.toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(String search, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<Product> productPage;

        boolean hasSearch = search != null && !search.isBlank();
        boolean hasCategory = categoryId != null;

        if (hasSearch && hasCategory) {
            productPage = productRepository.findByNameContainingIgnoreCaseAndCategoryId(search, categoryId, pageable);
        } else if (hasSearch) {
            productPage = productRepository.findByNameContainingIgnoreCase(search, pageable);
        } else if (hasCategory) {
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        List<ProductResponse> content = productPage.getContent().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());

        return PageResponse.of(productPage, content);
    }

    @Override
    @Transactional
    public void toggleProductStatus(Long id, String currentUserEmail) {
        ensureAdmin(currentUserEmail);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setStatus(product.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        productRepository.save(product);
    }
}
