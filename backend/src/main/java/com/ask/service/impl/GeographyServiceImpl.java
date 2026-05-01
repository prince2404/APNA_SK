package com.ask.service.impl;

import com.ask.constants.AppConstants;
import com.ask.dto.request.geography.*;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.geography.*;
import com.ask.entity.*;
import com.ask.enums.EntityStatus;
import com.ask.exception.DuplicateResourceException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.mapper.GeographyMapper;
import com.ask.repository.*;
import com.ask.service.GeographyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of GeographyService for managing the 4-tier geographic hierarchy.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeographyServiceImpl implements GeographyService {

    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final BlockRepository blockRepository;
    private final StoreRepository storeRepository;
    private final GeographyMapper geographyMapper;

    // ========== STATES ==========

    @Override
    @Transactional
    public StateResponse createState(StateRequest request) {
        if (stateRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("State already exists with code: " + request.getCode());
        }
        State state = State.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .build();
        return geographyMapper.toStateResponse(stateRepository.save(state));
    }

    @Override
    @Transactional(readOnly = true)
    public StateResponse getStateById(Long id) {
        State state = stateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("State", "id", id));
        return geographyMapper.toStateResponse(state);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StateResponse> getAllStates(int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Page<State> statePage = stateRepository.findAll(
                PageRequest.of(page, size, Sort.by("name").ascending()));
        List<StateResponse> content = statePage.getContent().stream()
                .map(geographyMapper::toStateResponse).toList();
        return PageResponse.of(statePage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StateResponse> getAllActiveStates() {
        return stateRepository.findByStatusOrderByNameAsc(EntityStatus.ACTIVE).stream()
                .map(geographyMapper::toStateResponse).toList();
    }

    @Override
    @Transactional
    public StateResponse updateState(Long id, StateRequest request) {
        State state = stateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("State", "id", id));
        state.setName(request.getName());
        state.setCode(request.getCode().toUpperCase());
        return geographyMapper.toStateResponse(stateRepository.save(state));
    }

    @Override
    @Transactional
    public void toggleStateStatus(Long id) {
        State state = stateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("State", "id", id));
        state.setStatus(state.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        stateRepository.save(state);
    }

    // ========== DISTRICTS ==========

    @Override
    @Transactional
    public DistrictResponse createDistrict(DistrictRequest request) {
        State state = stateRepository.findById(request.getStateId())
                .orElseThrow(() -> new ResourceNotFoundException("State", "id", request.getStateId()));
        if (districtRepository.existsByNameIgnoreCaseAndStateId(request.getName(), request.getStateId())) {
            throw new DuplicateResourceException("District already exists with this name in the state");
        }
        District district = District.builder().name(request.getName()).state(state).build();
        return geographyMapper.toDistrictResponse(districtRepository.save(district));
    }

    @Override
    @Transactional(readOnly = true)
    public DistrictResponse getDistrictById(Long id) {
        District district = districtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", id));
        return geographyMapper.toDistrictResponse(district);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DistrictResponse> getDistrictsByState(Long stateId, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Page<District> districtPage = districtRepository.findByStateIdAndStatus(
                stateId, EntityStatus.ACTIVE, PageRequest.of(page, size, Sort.by("name").ascending()));
        List<DistrictResponse> content = districtPage.getContent().stream()
                .map(geographyMapper::toDistrictResponse).toList();
        return PageResponse.of(districtPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DistrictResponse> getActiveDistrictsByState(Long stateId) {
        return districtRepository.findByStateIdAndStatusOrderByNameAsc(stateId, EntityStatus.ACTIVE).stream()
                .map(geographyMapper::toDistrictResponse).toList();
    }

    @Override
    @Transactional
    public DistrictResponse updateDistrict(Long id, DistrictRequest request) {
        District district = districtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", id));
        district.setName(request.getName());
        return geographyMapper.toDistrictResponse(districtRepository.save(district));
    }

    @Override
    @Transactional
    public void toggleDistrictStatus(Long id) {
        District district = districtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", id));
        district.setStatus(district.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        districtRepository.save(district);
    }

    // ========== BLOCKS ==========

    @Override
    @Transactional
    public BlockResponse createBlock(BlockRequest request) {
        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", request.getDistrictId()));
        if (blockRepository.existsByNameIgnoreCaseAndDistrictId(request.getName(), request.getDistrictId())) {
            throw new DuplicateResourceException("Block already exists with this name in the district");
        }
        Block block = Block.builder().name(request.getName()).district(district).build();
        return geographyMapper.toBlockResponse(blockRepository.save(block));
    }

    @Override
    @Transactional(readOnly = true)
    public BlockResponse getBlockById(Long id) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Block", "id", id));
        return geographyMapper.toBlockResponse(block);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BlockResponse> getBlocksByDistrict(Long districtId, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Page<Block> blockPage = blockRepository.findByDistrictIdAndStatus(
                districtId, EntityStatus.ACTIVE, PageRequest.of(page, size, Sort.by("name").ascending()));
        List<BlockResponse> content = blockPage.getContent().stream()
                .map(geographyMapper::toBlockResponse).toList();
        return PageResponse.of(blockPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockResponse> getActiveBlocksByDistrict(Long districtId) {
        return blockRepository.findByDistrictIdAndStatusOrderByNameAsc(districtId, EntityStatus.ACTIVE).stream()
                .map(geographyMapper::toBlockResponse).toList();
    }

    @Override
    @Transactional
    public BlockResponse updateBlock(Long id, BlockRequest request) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Block", "id", id));
        block.setName(request.getName());
        return geographyMapper.toBlockResponse(blockRepository.save(block));
    }

    @Override
    @Transactional
    public void toggleBlockStatus(Long id) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Block", "id", id));
        block.setStatus(block.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        blockRepository.save(block);
    }

    // ========== STORES ==========

    @Override
    @Transactional
    public StoreResponse createStore(StoreRequest request) {
        Block block = blockRepository.findById(request.getBlockId())
                .orElseThrow(() -> new ResourceNotFoundException("Block", "id", request.getBlockId()));
        if (storeRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Store already exists with code: " + request.getCode());
        }
        Store store = Store.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .address(request.getAddress())
                .phone(request.getPhone())
                .operatingHours(request.getOperatingHours())
                .block(block)
                .build();
        return geographyMapper.toStoreResponse(storeRepository.save(store));
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponse getStoreById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));
        return geographyMapper.toStoreResponse(store);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StoreResponse> getStoresByBlock(Long blockId, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Page<Store> storePage = storeRepository.findByBlockIdAndStatus(
                blockId, EntityStatus.ACTIVE, PageRequest.of(page, size, Sort.by("name").ascending()));
        List<StoreResponse> content = storePage.getContent().stream()
                .map(geographyMapper::toStoreResponse).toList();
        return PageResponse.of(storePage, content);
    }

    @Override
    @Transactional
    public StoreResponse updateStore(Long id, StoreRequest request) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));
        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());
        store.setOperatingHours(request.getOperatingHours());
        return geographyMapper.toStoreResponse(storeRepository.save(store));
    }

    @Override
    @Transactional
    public void toggleStoreStatus(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));
        store.setStatus(store.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        storeRepository.save(store);
    }
}
