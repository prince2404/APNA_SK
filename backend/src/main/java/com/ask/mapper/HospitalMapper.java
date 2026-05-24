package com.ask.mapper;

import com.ask.dto.response.hospital.HospitalResponse;
import com.ask.entity.Hospital;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HospitalMapper {

    @Mapping(source = "state.id", target = "stateId")
    @Mapping(source = "state.name", target = "stateName")
    @Mapping(source = "district.id", target = "districtId")
    @Mapping(source = "district.name", target = "districtName")
    HospitalResponse toHospitalResponse(Hospital hospital);
}
