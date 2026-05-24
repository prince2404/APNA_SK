package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.request.product.CategoryRequest;
import com.ask.dto.request.product.ProductRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.product.ProductCategoryResponse;
import com.ask.dto.response.product.ProductResponse;
import com.ask.entity.Product;
import com.ask.entity.ProductCategory;
import com.ask.entity.Role;
import com.ask.entity.User;
import com.ask.enums.EntityStatus;
import org.springframework.security.access.AccessDeniedException;
import com.ask.exception.BusinessRuleException;
import com.ask.mapper.ProductMapper;
import com.ask.repository.ProductCategoryRepository;
import com.ask.repository.ProductRepository;
import com.ask.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductCategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private User adminUser;
    private User regularUser;
    private Role adminRole;
    private Role regularRole;
    private ProductCategory category;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder().name(RoleConstants.SUPER_ADMIN).hierarchyLevel(1).build();
        regularRole = Role.builder().name(RoleConstants.RECEPTIONIST).hierarchyLevel(6).build();

        adminUser = User.builder().email("admin@ask.in").role(adminRole).build();
        regularUser = User.builder().email("staff@ask.in").role(regularRole).build();

        category = ProductCategory.builder().id(1L).name("Medicine").status(EntityStatus.ACTIVE).build();
    }

    @Test
    void createCategorySucceedsForAdmin() {
        CategoryRequest req = CategoryRequest.builder().name("Medicine").build();

        when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(categoryRepository.findByName(req.getName())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(ProductCategory.class))).thenReturn(category);
        when(productMapper.toCategoryResponse(category))
                .thenReturn(ProductCategoryResponse.builder().id(1L).name("Medicine").status(EntityStatus.ACTIVE).build());

        ProductCategoryResponse resp = productService.createCategory(req, adminUser.getEmail());

        assertNotNull(resp);
        assertEquals("Medicine", resp.getName());
        verify(categoryRepository, times(1)).save(any(ProductCategory.class));
    }

    @Test
    void createCategoryThrowsAccessDeniedForNonAdmin() {
        CategoryRequest req = CategoryRequest.builder().name("Medicine").build();
        when(userRepository.findByEmail(regularUser.getEmail())).thenReturn(Optional.of(regularUser));

        assertThrows(AccessDeniedException.class, () -> {
            productService.createCategory(req, regularUser.getEmail());
        });
    }

    @Test
    void createProductThrowsForInvalidPrice() {
        ProductRequest req = ProductRequest.builder()
                .name("Paracetamol")
                .categoryId(1L)
                .mrp(new BigDecimal("10.00"))
                .askPrice(new BigDecimal("15.00")) // ask > mrp
                .gstPercentage(new BigDecimal("18.00"))
                .minStockThreshold(10)
                .build();

        when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));

        assertThrows(BusinessRuleException.class, () -> {
            productService.createProduct(req, adminUser.getEmail());
        });
    }

    @Test
    void createProductSucceeds() {
        ProductRequest req = ProductRequest.builder()
                .name("Paracetamol")
                .categoryId(1L)
                .mrp(new BigDecimal("10.00"))
                .askPrice(new BigDecimal("8.00"))
                .gstPercentage(new BigDecimal("18.00"))
                .minStockThreshold(10)
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Paracetamol")
                .mrp(new BigDecimal("10.00"))
                .askPrice(new BigDecimal("8.00"))
                .category(category)
                .build();

        when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toProductResponse(product))
                .thenReturn(ProductResponse.builder().id(1L).name("Paracetamol").askPrice(new BigDecimal("8.00")).build());

        ProductResponse resp = productService.createProduct(req, adminUser.getEmail());

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals("Paracetamol", resp.getName());
    }

    @Test
    void getProductsFiltersCorrectly() {
        Product p1 = Product.builder().id(1L).name("Paracetamol").build();
        ProductResponse pr1 = ProductResponse.builder().id(1L).name("Paracetamol").build();
        Page<Product> page = new PageImpl<>(List.of(p1));

        when(productRepository.findByNameContainingIgnoreCase(eq("para"), any(Pageable.class))).thenReturn(page);
        when(productMapper.toProductResponse(p1)).thenReturn(pr1);

        PageResponse<ProductResponse> resp = productService.getProducts("para", null, 0, 10);

        assertNotNull(resp);
        assertEquals(1, resp.getContent().size());
        assertEquals("Paracetamol", resp.getContent().get(0).getName());
    }
}
