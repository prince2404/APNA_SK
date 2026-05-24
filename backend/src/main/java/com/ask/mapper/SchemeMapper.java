package com.ask.mapper;

import com.ask.dto.response.billing.SchemeResponse;
import com.ask.entity.Scheme;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SchemeMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "state.id", target = "stateId")
    @Mapping(source = "state.name", target = "stateName")
    @Mapping(source = "createdBy.id", target = "createdBy")
    @Mapping(source = "createdBy.fullName", target = "createdByName")
    SchemeResponse toSchemeResponse(Scheme scheme);
}
