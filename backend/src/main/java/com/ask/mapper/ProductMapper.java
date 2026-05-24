package com.ask.mapper;

import com.ask.dto.response.product.ProductCategoryResponse;
import com.ask.dto.response.product.ProductResponse;
import com.ask.entity.Product;
import com.ask.entity.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for product catalog entities.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductResponse toProductResponse(Product product);

    ProductCategoryResponse toCategoryResponse(ProductCategory category);
}
