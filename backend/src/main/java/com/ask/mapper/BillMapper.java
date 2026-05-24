package com.ask.mapper;

import com.ask.dto.response.billing.BillItemResponse;
import com.ask.dto.response.billing.BillResponse;
import com.ask.entity.Bill;
import com.ask.entity.BillItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BillMapper {

    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "patient.fullName", target = "patientName")
    @Mapping(source = "patient.phone", target = "patientPhone")
    @Mapping(source = "healthCard.id", target = "healthCardId")
    @Mapping(source = "healthCard.cardNumber", target = "healthCardNumber")
    @Mapping(source = "cancelledBy.id", target = "cancelledBy")
    @Mapping(source = "cancelledBy.fullName", target = "cancelledByName")
    @Mapping(source = "createdBy.id", target = "createdBy")
    @Mapping(source = "createdBy.fullName", target = "createdByName")
    BillResponse toBillResponse(Bill bill);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.brand", target = "productBrand")
    BillItemResponse toBillItemResponse(BillItem item);
}
