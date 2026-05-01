package com.ask.service.impl;

import com.ask.constants.AppConstants;
import com.ask.constants.RoleConstants;
import com.ask.dto.request.geography.*;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.geography.*;
import com.ask.entity.*;
import com.ask.enums.EntityStatus;
import com.ask.exception.DuplicateResourceException;
import com.ask.exception.GeographicScopeException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.mapper.GeographyMapper;
import com.ask.repository.*;
import com.ask.service.AuditService;
import com.ask.service.GeographyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserRepository userRepository;
    private final GeographyMapper geographyMapper;
    private final AuditService auditService;

    // ========== STATES ==========

    @Override
    @Transactional
    public StateResponse createState(StateRequest request) {
        User currentUser = getCurrentUser();
        if (stateRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("State already exists with code: " + request.getCode());
        }
        State state = State.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .build();
        State saved = stateRepository.save(state);
        auditService.log(currentUser, "CREATE_STATE", "STATE", saved.getId(), null, null, null,
                "Created state: " + saved.getName());
        return geographyMapper.toStateResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StateResponse getStateById(Long id) {
        State state = getStateInScope(id);
        return geographyMapper.toStateResponse(state);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StateResponse> getAllStates(int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        User currentUser = getCurrentUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<State> statePage = hasPlatformScope(currentUser)
                ? stateRepository.findAll(pageRequest)
                : stateRepository.findByIdAndStatus(requireStateId(currentUser), EntityStatus.ACTIVE, pageRequest);
        List<StateResponse> content = statePage.getContent().stream()
                .map(geographyMapper::toStateResponse).toList();
        return PageResponse.of(statePage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StateResponse> getAllActiveStates(int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        User currentUser = getCurrentUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<State> statePage = hasPlatformScope(currentUser)
                ? stateRepository.findByStatus(EntityStatus.ACTIVE, pageRequest)
                : stateRepository.findByIdAndStatus(requireStateId(currentUser), EntityStatus.ACTIVE, pageRequest);
        List<StateResponse> content = statePage.getContent().stream()
                .map(geographyMapper::toStateResponse).toList();
        return PageResponse.of(statePage, content);
    }

    @Override
    @Transactional
    public StateResponse updateState(Long id, StateRequest request) {
        User currentUser = getCurrentUser();
        State state = getStateInScope(id);
        state.setName(request.getName());
        state.setCode(request.getCode().toUpperCase());
        State saved = stateRepository.save(state);
        auditService.log(currentUser, "UPDATE_STATE", "STATE", saved.getId(), null, null, null,
                "Updated state: " + saved.getName());
        return geographyMapper.toStateResponse(saved);
    }

    @Override
    @Transactional
    public void toggleStateStatus(Long id) {
        User currentUser = getCurrentUser();
        State state = getStateInScope(id);
        state.setStatus(state.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        stateRepository.save(state);
        auditService.log(currentUser, "TOGGLE_STATE_STATUS", "STATE", state.getId(), null, null, null,
                "Toggled state status to: " + state.getStatus());
    }

    // ========== DISTRICTS ==========

    @Override
    @Transactional
    public DistrictResponse createDistrict(DistrictRequest request) {
        User currentUser = getCurrentUser();
        State state = getStateInScope(request.getStateId());
        if (districtRepository.existsByNameIgnoreCaseAndStateId(request.getName(), request.getStateId())) {
            throw new DuplicateResourceException("District already exists with this name in the state");
        }
        District district = District.builder().name(request.getName()).state(state).build();
        District saved = districtRepository.save(district);
        auditService.log(currentUser, "CREATE_DISTRICT", "DISTRICT", saved.getId(), null, null, null,
                "Created district: " + saved.getName());
        return geographyMapper.toDistrictResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DistrictResponse getDistrictById(Long id) {
        District district = getDistrictInScope(id);
        return geographyMapper.toDistrictResponse(district);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DistrictResponse> getDistrictsByState(Long stateId, int page, int size) {
        User currentUser = getCurrentUser();
        getStateInScope(stateId);
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<District> districtPage = getScopedDistrictId(currentUser) != null
                ? districtRepository.findByIdAndStatus(getScopedDistrictId(currentUser), EntityStatus.ACTIVE, pageRequest)
                : districtRepository.findByStateIdAndStatus(stateId, EntityStatus.ACTIVE, pageRequest);
        List<DistrictResponse> content = districtPage.getContent().stream()
                .map(geographyMapper::toDistrictResponse).toList();
        return PageResponse.of(districtPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DistrictResponse> getActiveDistrictsByState(Long stateId, int page, int size) {
        return getDistrictsByState(stateId, page, size);
    }

    @Override
    @Transactional
    public DistrictResponse updateDistrict(Long id, DistrictRequest request) {
        User currentUser = getCurrentUser();
        District district = getDistrictInScope(id);
        State state = getStateInScope(request.getStateId());
        district.setName(request.getName());
        district.setState(state);
        District saved = districtRepository.save(district);
        auditService.log(currentUser, "UPDATE_DISTRICT", "DISTRICT", saved.getId(), null, null, null,
                "Updated district: " + saved.getName());
        return geographyMapper.toDistrictResponse(saved);
    }

    @Override
    @Transactional
    public void toggleDistrictStatus(Long id) {
        User currentUser = getCurrentUser();
        District district = getDistrictInScope(id);
        district.setStatus(district.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        districtRepository.save(district);
        auditService.log(currentUser, "TOGGLE_DISTRICT_STATUS", "DISTRICT", district.getId(), null, null, null,
                "Toggled district status to: " + district.getStatus());
    }

    // ========== BLOCKS ==========

    @Override
    @Transactional
    public BlockResponse createBlock(BlockRequest request) {
        User currentUser = getCurrentUser();
        District district = getDistrictInScope(request.getDistrictId());
        if (blockRepository.existsByNameIgnoreCaseAndDistrictId(request.getName(), request.getDistrictId())) {
            throw new DuplicateResourceException("Block already exists with this name in the district");
        }
        Block block = Block.builder().name(request.getName()).district(district).build();
        Block saved = blockRepository.save(block);
        auditService.log(currentUser, "CREATE_BLOCK", "BLOCK", saved.getId(), null, null, null,
                "Created block: " + saved.getName());
        return geographyMapper.toBlockResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BlockResponse getBlockById(Long id) {
        Block block = getBlockInScope(id);
        return geographyMapper.toBlockResponse(block);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BlockResponse> getBlocksByDistrict(Long districtId, int page, int size) {
        User currentUser = getCurrentUser();
        getDistrictInScope(districtId);
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Block> blockPage = getScopedBlockId(currentUser) != null
                ? blockRepository.findByIdAndStatus(getScopedBlockId(currentUser), EntityStatus.ACTIVE, pageRequest)
                : blockRepository.findByDistrictIdAndStatus(districtId, EntityStatus.ACTIVE, pageRequest);
        List<BlockResponse> content = blockPage.getContent().stream()
                .map(geographyMapper::toBlockResponse).toList();
        return PageResponse.of(blockPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BlockResponse> getActiveBlocksByDistrict(Long districtId, int page, int size) {
        return getBlocksByDistrict(districtId, page, size);
    }

    @Override
    @Transactional
    public BlockResponse updateBlock(Long id, BlockRequest request) {
        User currentUser = getCurrentUser();
        Block block = getBlockInScope(id);
        District district = getDistrictInScope(request.getDistrictId());
        block.setName(request.getName());
        block.setDistrict(district);
        Block saved = blockRepository.save(block);
        auditService.log(currentUser, "UPDATE_BLOCK", "BLOCK", saved.getId(), null, null, null,
                "Updated block: " + saved.getName());
        return geographyMapper.toBlockResponse(saved);
    }

    @Override
    @Transactional
    public void toggleBlockStatus(Long id) {
        User currentUser = getCurrentUser();
        Block block = getBlockInScope(id);
        block.setStatus(block.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        blockRepository.save(block);
        auditService.log(currentUser, "TOGGLE_BLOCK_STATUS", "BLOCK", block.getId(), null, null, null,
                "Toggled block status to: " + block.getStatus());
    }

    // ========== STORES ==========

    @Override
    @Transactional
    public StoreResponse createStore(StoreRequest request) {
        User currentUser = getCurrentUser();
        Block block = getBlockInScope(request.getBlockId());
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
        Store saved = storeRepository.save(store);
        auditService.log(currentUser, "CREATE_STORE", "STORE", saved.getId(), null, null, null,
                "Created store: " + saved.getName());
        return geographyMapper.toStoreResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponse getStoreById(Long id) {
        Store store = getStoreInScope(id);
        return geographyMapper.toStoreResponse(store);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StoreResponse> getStoresByBlock(Long blockId, int page, int size) {
        User currentUser = getCurrentUser();
        getBlockInScope(blockId);
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Store> storePage = getScopedStoreId(currentUser) != null
                ? storeRepository.findByIdAndStatus(getScopedStoreId(currentUser), EntityStatus.ACTIVE, pageRequest)
                : storeRepository.findByBlockIdAndStatus(blockId, EntityStatus.ACTIVE, pageRequest);
        List<StoreResponse> content = storePage.getContent().stream()
                .map(geographyMapper::toStoreResponse).toList();
        return PageResponse.of(storePage, content);
    }

    @Override
    @Transactional
    public StoreResponse updateStore(Long id, StoreRequest request) {
        User currentUser = getCurrentUser();
        Store store = getStoreInScope(id);
        Block block = getBlockInScope(request.getBlockId());
        String code = request.getCode().toUpperCase();
        if (storeRepository.existsByCodeAndIdNot(code, id)) {
            throw new DuplicateResourceException("Store already exists with code: " + request.getCode());
        }
        store.setName(request.getName());
        store.setCode(code);
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());
        store.setOperatingHours(request.getOperatingHours());
        store.setBlock(block);
        Store saved = storeRepository.save(store);
        auditService.log(currentUser, "UPDATE_STORE", "STORE", saved.getId(), null, null, null,
                "Updated store: " + saved.getName());
        return geographyMapper.toStoreResponse(saved);
    }

    @Override
    @Transactional
    public void toggleStoreStatus(Long id) {
        User currentUser = getCurrentUser();
        Store store = getStoreInScope(id);
        store.setStatus(store.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        storeRepository.save(store);
        auditService.log(currentUser, "TOGGLE_STORE_STATUS", "STORE", store.getId(), null, null, null,
                "Toggled store status to: " + store.getStatus());
    }

    // ========== SCOPE HELPERS ==========

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new GeographicScopeException();
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authentication.getName()));
    }

    private boolean hasPlatformScope(User user) {
        String roleName = user.getRole().getName();
        return RoleConstants.SUPER_ADMIN.equals(roleName) || RoleConstants.SYSTEM_ADMIN.equals(roleName);
    }

    private Long requireStateId(User user) {
        if (user.getState() == null) {
            throw new GeographicScopeException();
        }
        return user.getState().getId();
    }

    private Long getScopedDistrictId(User user) {
        if (user.getDistrict() != null) {
            return user.getDistrict().getId();
        }
        if (user.getBlock() != null) {
            return user.getBlock().getDistrict().getId();
        }
        if (user.getStore() != null) {
            return user.getStore().getBlock().getDistrict().getId();
        }
        return null;
    }

    private Long getScopedBlockId(User user) {
        if (user.getBlock() != null) {
            return user.getBlock().getId();
        }
        if (user.getStore() != null) {
            return user.getStore().getBlock().getId();
        }
        return null;
    }

    private Long getScopedStoreId(User user) {
        return user.getStore() != null ? user.getStore().getId() : null;
    }

    private State getStateInScope(Long stateId) {
        User currentUser = getCurrentUser();
        if (!hasPlatformScope(currentUser) && !requireStateId(currentUser).equals(stateId)) {
            throw new GeographicScopeException();
        }
        return stateRepository.findById(stateId)
                .orElseThrow(() -> new ResourceNotFoundException("State", "id", stateId));
    }

    private District getDistrictInScope(Long districtId) {
        User currentUser = getCurrentUser();
        if (hasPlatformScope(currentUser)) {
            return districtRepository.findById(districtId)
                    .orElseThrow(() -> new ResourceNotFoundException("District", "id", districtId));
        }
        return districtRepository.findByIdAndStateId(districtId, requireStateId(currentUser))
                .orElseThrow(GeographicScopeException::new);
    }

    private Block getBlockInScope(Long blockId) {
        User currentUser = getCurrentUser();
        if (hasPlatformScope(currentUser)) {
            return blockRepository.findById(blockId)
                    .orElseThrow(() -> new ResourceNotFoundException("Block", "id", blockId));
        }
        if (currentUser.getDistrict() != null) {
            return blockRepository.findByIdAndDistrictId(blockId, currentUser.getDistrict().getId())
                    .orElseThrow(GeographicScopeException::new);
        }
        return blockRepository.findByIdAndStateId(blockId, requireStateId(currentUser))
                .orElseThrow(GeographicScopeException::new);
    }

    private Store getStoreInScope(Long storeId) {
        User currentUser = getCurrentUser();
        if (hasPlatformScope(currentUser)) {
            return storeRepository.findById(storeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId));
        }
        if (currentUser.getStore() != null) {
            return storeRepository.findByIdAndBlockId(storeId, currentUser.getStore().getBlock().getId())
                    .filter(store -> store.getId().equals(currentUser.getStore().getId()))
                    .orElseThrow(GeographicScopeException::new);
        }
        if (currentUser.getBlock() != null) {
            return storeRepository.findByIdAndBlockId(storeId, currentUser.getBlock().getId())
                    .orElseThrow(GeographicScopeException::new);
        }
        if (currentUser.getDistrict() != null) {
            return storeRepository.findByIdAndDistrictId(storeId, currentUser.getDistrict().getId())
                    .orElseThrow(GeographicScopeException::new);
        }
        return storeRepository.findByIdAndStateId(storeId, requireStateId(currentUser))
                .orElseThrow(GeographicScopeException::new);
    }
}
