package com.ask.service;

import com.ask.dto.request.geography.*;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.geography.*;

import java.util.List;

/**
 * Service for managing geographic hierarchy: States, Districts, Blocks, Stores.
 */
public interface GeographyService {

    // --- States ---
    StateResponse createState(StateRequest request);
    StateResponse getStateById(Long id);
    PageResponse<StateResponse> getAllStates(int page, int size);
    List<StateResponse> getAllActiveStates();
    StateResponse updateState(Long id, StateRequest request);
    void toggleStateStatus(Long id);

    // --- Districts ---
    DistrictResponse createDistrict(DistrictRequest request);
    DistrictResponse getDistrictById(Long id);
    PageResponse<DistrictResponse> getDistrictsByState(Long stateId, int page, int size);
    List<DistrictResponse> getActiveDistrictsByState(Long stateId);
    DistrictResponse updateDistrict(Long id, DistrictRequest request);
    void toggleDistrictStatus(Long id);

    // --- Blocks ---
    BlockResponse createBlock(BlockRequest request);
    BlockResponse getBlockById(Long id);
    PageResponse<BlockResponse> getBlocksByDistrict(Long districtId, int page, int size);
    List<BlockResponse> getActiveBlocksByDistrict(Long districtId);
    BlockResponse updateBlock(Long id, BlockRequest request);
    void toggleBlockStatus(Long id);

    // --- Stores ---
    StoreResponse createStore(StoreRequest request);
    StoreResponse getStoreById(Long id);
    PageResponse<StoreResponse> getStoresByBlock(Long blockId, int page, int size);
    StoreResponse updateStore(Long id, StoreRequest request);
    void toggleStoreStatus(Long id);
}
