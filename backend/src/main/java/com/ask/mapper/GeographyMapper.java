package com.ask.mapper;

import com.ask.dto.response.geography.*;
import com.ask.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for geographic entities (State, District, Block, Store).
 * Converts entities to response DTOs with relationship data flattened.
 */
@Mapper(componentModel = "spring")
public interface GeographyMapper {

    @Mapping(target = "districtCount", expression = "java(state.getDistricts() != null ? state.getDistricts().size() : 0)")
    StateResponse toStateResponse(State state);

    @Mapping(source = "state.id", target = "stateId")
    @Mapping(source = "state.name", target = "stateName")
    @Mapping(target = "blockCount", expression = "java(district.getBlocks() != null ? district.getBlocks().size() : 0)")
    DistrictResponse toDistrictResponse(District district);

    @Mapping(source = "district.id", target = "districtId")
    @Mapping(source = "district.name", target = "districtName")
    @Mapping(source = "district.state.id", target = "stateId")
    @Mapping(source = "district.state.name", target = "stateName")
    @Mapping(target = "storeCount", expression = "java(block.getStores() != null ? block.getStores().size() : 0)")
    BlockResponse toBlockResponse(Block block);

    @Mapping(source = "block.id", target = "blockId")
    @Mapping(source = "block.name", target = "blockName")
    @Mapping(source = "block.district.id", target = "districtId")
    @Mapping(source = "block.district.name", target = "districtName")
    @Mapping(source = "block.district.state.id", target = "stateId")
    @Mapping(source = "block.district.state.name", target = "stateName")
    StoreResponse toStoreResponse(Store store);
}
