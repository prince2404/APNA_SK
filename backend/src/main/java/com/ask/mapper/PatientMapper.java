package com.ask.mapper;

import com.ask.dto.response.patient.PatientResponse;
import com.ask.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(source = "state.id", target = "stateId")
    @Mapping(source = "state.name", target = "stateName")
    @Mapping(source = "district.id", target = "districtId")
    @Mapping(source = "district.name", target = "districtName")
    @Mapping(source = "block.id", target = "blockId")
    @Mapping(source = "block.name", target = "blockName")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "hospital.id", target = "hospitalId")
    @Mapping(source = "hospital.name", target = "hospitalName")
    PatientResponse toPatientResponse(Patient patient);
}
