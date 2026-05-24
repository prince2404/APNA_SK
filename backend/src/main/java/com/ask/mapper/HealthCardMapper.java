package com.ask.mapper;

import com.ask.dto.response.healthcard.HealthCardMemberResponse;
import com.ask.dto.response.healthcard.HealthCardResponse;
import com.ask.entity.HealthCard;
import com.ask.entity.HealthCardMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HealthCardMapper {

    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "patient.fullName", target = "patientName")
    @Mapping(source = "patient.phone", target = "patientPhone")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "issuedBy.id", target = "issuedBy")
    @Mapping(source = "issuedBy.fullName", target = "issuedByName")
    HealthCardResponse toHealthCardResponse(HealthCard card);

    HealthCardMemberResponse toMemberResponse(HealthCardMember member);
}
