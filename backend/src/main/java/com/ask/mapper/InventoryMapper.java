package com.ask.mapper;

import com.ask.dto.response.inventory.*;
import com.ask.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for stock, warehouse receipts, transfer orders, and stock adjustments.
 */
@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "receivedBy.email", target = "receivedByEmail")
    StockCentralResponse toStockCentralResponse(StockCentral stock);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.brand", target = "productBrand")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.name", target = "storeName")
    StockStoreResponse toStockStoreResponse(StockStore stock);

    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "createdBy.email", target = "createdByEmail")
    @Mapping(source = "confirmedBy.email", target = "confirmedByEmail")
    TransferOrderResponse toTransferOrderResponse(TransferOrder order);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    TransferOrderItemResponse toTransferOrderItemResponse(TransferOrderItem item);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "adjustedBy.email", target = "adjustedByEmail")
    StockAdjustmentResponse toStockAdjustmentResponse(StockAdjustment adjustment);
}
