package com.ask.mapper;

import com.ask.dto.response.inventory.StockRequestResponse;
import com.ask.entity.StockRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for store replenishment requests.
 */
@Mapper(componentModel = "spring")
public interface StockRequestMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "requestedBy.email", target = "requestedByEmail")
    @Mapping(source = "reviewedBy.email", target = "reviewedByEmail")
    StockRequestResponse toStockRequestResponse(StockRequest request);
}
