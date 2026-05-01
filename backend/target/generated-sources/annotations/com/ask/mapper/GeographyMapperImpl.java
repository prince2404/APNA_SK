package com.ask.mapper;

import com.ask.dto.response.geography.BlockResponse;
import com.ask.dto.response.geography.DistrictResponse;
import com.ask.dto.response.geography.StateResponse;
import com.ask.dto.response.geography.StoreResponse;
import com.ask.entity.Block;
import com.ask.entity.District;
import com.ask.entity.State;
import com.ask.entity.Store;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-01T18:11:49+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class GeographyMapperImpl implements GeographyMapper {

    @Override
    public StateResponse toStateResponse(State state) {
        if ( state == null ) {
            return null;
        }

        StateResponse.StateResponseBuilder stateResponse = StateResponse.builder();

        stateResponse.code( state.getCode() );
        stateResponse.id( state.getId() );
        stateResponse.name( state.getName() );
        if ( state.getStatus() != null ) {
            stateResponse.status( state.getStatus().name() );
        }

        stateResponse.districtCount( state.getDistricts() != null ? state.getDistricts().size() : 0 );

        return stateResponse.build();
    }

    @Override
    public DistrictResponse toDistrictResponse(District district) {
        if ( district == null ) {
            return null;
        }

        DistrictResponse.DistrictResponseBuilder districtResponse = DistrictResponse.builder();

        districtResponse.stateId( districtStateId( district ) );
        districtResponse.stateName( districtStateName( district ) );
        districtResponse.id( district.getId() );
        districtResponse.name( district.getName() );
        if ( district.getStatus() != null ) {
            districtResponse.status( district.getStatus().name() );
        }

        districtResponse.blockCount( district.getBlocks() != null ? district.getBlocks().size() : 0 );

        return districtResponse.build();
    }

    @Override
    public BlockResponse toBlockResponse(Block block) {
        if ( block == null ) {
            return null;
        }

        BlockResponse.BlockResponseBuilder blockResponse = BlockResponse.builder();

        blockResponse.districtId( blockDistrictId( block ) );
        blockResponse.districtName( blockDistrictName( block ) );
        blockResponse.stateId( blockDistrictStateId( block ) );
        blockResponse.stateName( blockDistrictStateName( block ) );
        blockResponse.id( block.getId() );
        blockResponse.name( block.getName() );
        if ( block.getStatus() != null ) {
            blockResponse.status( block.getStatus().name() );
        }

        blockResponse.storeCount( block.getStores() != null ? block.getStores().size() : 0 );

        return blockResponse.build();
    }

    @Override
    public StoreResponse toStoreResponse(Store store) {
        if ( store == null ) {
            return null;
        }

        StoreResponse.StoreResponseBuilder storeResponse = StoreResponse.builder();

        storeResponse.blockId( storeBlockId( store ) );
        storeResponse.blockName( storeBlockName( store ) );
        storeResponse.districtId( storeBlockDistrictId( store ) );
        storeResponse.districtName( storeBlockDistrictName( store ) );
        storeResponse.stateId( storeBlockDistrictStateId( store ) );
        storeResponse.stateName( storeBlockDistrictStateName( store ) );
        storeResponse.address( store.getAddress() );
        storeResponse.code( store.getCode() );
        storeResponse.id( store.getId() );
        storeResponse.name( store.getName() );
        storeResponse.operatingHours( store.getOperatingHours() );
        storeResponse.phone( store.getPhone() );
        if ( store.getStatus() != null ) {
            storeResponse.status( store.getStatus().name() );
        }

        return storeResponse.build();
    }

    private Long districtStateId(District district) {
        State state = district.getState();
        if ( state == null ) {
            return null;
        }
        return state.getId();
    }

    private String districtStateName(District district) {
        State state = district.getState();
        if ( state == null ) {
            return null;
        }
        return state.getName();
    }

    private Long blockDistrictId(Block block) {
        District district = block.getDistrict();
        if ( district == null ) {
            return null;
        }
        return district.getId();
    }

    private String blockDistrictName(Block block) {
        District district = block.getDistrict();
        if ( district == null ) {
            return null;
        }
        return district.getName();
    }

    private Long blockDistrictStateId(Block block) {
        District district = block.getDistrict();
        if ( district == null ) {
            return null;
        }
        State state = district.getState();
        if ( state == null ) {
            return null;
        }
        return state.getId();
    }

    private String blockDistrictStateName(Block block) {
        District district = block.getDistrict();
        if ( district == null ) {
            return null;
        }
        State state = district.getState();
        if ( state == null ) {
            return null;
        }
        return state.getName();
    }

    private Long storeBlockId(Store store) {
        Block block = store.getBlock();
        if ( block == null ) {
            return null;
        }
        return block.getId();
    }

    private String storeBlockName(Store store) {
        Block block = store.getBlock();
        if ( block == null ) {
            return null;
        }
        return block.getName();
    }

    private Long storeBlockDistrictId(Store store) {
        Block block = store.getBlock();
        if ( block == null ) {
            return null;
        }
        District district = block.getDistrict();
        if ( district == null ) {
            return null;
        }
        return district.getId();
    }

    private String storeBlockDistrictName(Store store) {
        Block block = store.getBlock();
        if ( block == null ) {
            return null;
        }
        District district = block.getDistrict();
        if ( district == null ) {
            return null;
        }
        return district.getName();
    }

    private Long storeBlockDistrictStateId(Store store) {
        Block block = store.getBlock();
        if ( block == null ) {
            return null;
        }
        District district = block.getDistrict();
        if ( district == null ) {
            return null;
        }
        State state = district.getState();
        if ( state == null ) {
            return null;
        }
        return state.getId();
    }

    private String storeBlockDistrictStateName(Store store) {
        Block block = store.getBlock();
        if ( block == null ) {
            return null;
        }
        District district = block.getDistrict();
        if ( district == null ) {
            return null;
        }
        State state = district.getState();
        if ( state == null ) {
            return null;
        }
        return state.getName();
    }
}
