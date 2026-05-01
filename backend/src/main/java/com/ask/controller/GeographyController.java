package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.constants.AppConstants;
import com.ask.dto.request.geography.*;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.geography.*;
import com.ask.service.GeographyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing the geographic hierarchy (States, Districts, Blocks, Stores).
 * Only SUPER_ADMIN and SYSTEM_ADMIN with GEOGRAPHY:MANAGE permission can create/edit.
 * Users with GEOGRAPHY:VIEW can read geographic data.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class GeographyController {

    private final GeographyService geographyService;

    // ==================== STATES ====================

    /** POST /api/v1/states — Create a new state. Accessible by: SUPER_ADMIN, SYSTEM_ADMIN */
    @PostMapping(ApiPaths.STATES)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<StateResponse>> createState(
            @Valid @RequestBody StateRequest request) {
        StateResponse response = geographyService.createState(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "State created successfully", ApiPaths.STATES));
    }

    /** GET /api/v1/states/{id} — Get state by ID. Accessible by: All authenticated users */
    @GetMapping(ApiPaths.STATES + "/{id}")
    public ResponseEntity<ApiResponse<StateResponse>> getState(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.getStateById(id), ApiPaths.STATES + "/" + id));
    }

    /** GET /api/v1/states — Get all states (paginated). Accessible by: All authenticated users */
    @GetMapping(ApiPaths.STATES)
    public ResponseEntity<ApiResponse<PageResponse<StateResponse>>> getAllStates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.getAllStates(page, size), ApiPaths.STATES));
    }

    /** GET /api/v1/states/active — Get all active states (dropdown). Accessible by: All authenticated */
    @GetMapping(ApiPaths.STATES + "/active")
    public ResponseEntity<ApiResponse<List<StateResponse>>> getActiveStates() {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.getAllActiveStates(), ApiPaths.STATES + "/active"));
    }

    /** PUT /api/v1/states/{id} — Update state. Accessible by: SUPER_ADMIN, SYSTEM_ADMIN */
    @PutMapping(ApiPaths.STATES + "/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<StateResponse>> updateState(
            @PathVariable Long id, @Valid @RequestBody StateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.updateState(id, request), "State updated successfully", ApiPaths.STATES + "/" + id));
    }

    /** PATCH /api/v1/states/{id}/toggle — Toggle state status. Accessible by: SUPER_ADMIN */
    @PatchMapping(ApiPaths.STATES + "/{id}/toggle")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleState(@PathVariable Long id) {
        geographyService.toggleStateStatus(id);
        return ResponseEntity.ok(ApiResponse.success(null, "State status toggled", ApiPaths.STATES + "/" + id + "/toggle"));
    }

    // ==================== DISTRICTS ====================

    /** POST /api/v1/districts — Create district. Accessible by: SUPER_ADMIN, SYSTEM_ADMIN */
    @PostMapping(ApiPaths.DISTRICTS)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<DistrictResponse>> createDistrict(
            @Valid @RequestBody DistrictRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                geographyService.createDistrict(request), "District created successfully", ApiPaths.DISTRICTS));
    }

    /** GET /api/v1/districts/{id} — Get district by ID. Accessible by: All authenticated */
    @GetMapping(ApiPaths.DISTRICTS + "/{id}")
    public ResponseEntity<ApiResponse<DistrictResponse>> getDistrict(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.getDistrictById(id), ApiPaths.DISTRICTS + "/" + id));
    }

    /** GET /api/v1/districts?stateId=1 — Get districts by state (paginated). Accessible by: All */
    @GetMapping(ApiPaths.DISTRICTS)
    public ResponseEntity<ApiResponse<PageResponse<DistrictResponse>>> getDistrictsByState(
            @RequestParam Long stateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.getDistrictsByState(stateId, page, size), ApiPaths.DISTRICTS));
    }

    /** GET /api/v1/districts/active?stateId=1 — Active districts for dropdown. Accessible by: All */
    @GetMapping(ApiPaths.DISTRICTS + "/active")
    public ResponseEntity<ApiResponse<List<DistrictResponse>>> getActiveDistricts(
            @RequestParam Long stateId) {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.getActiveDistrictsByState(stateId), ApiPaths.DISTRICTS + "/active"));
    }

    /** PUT /api/v1/districts/{id} — Update district. Accessible by: SUPER_ADMIN, SYSTEM_ADMIN */
    @PutMapping(ApiPaths.DISTRICTS + "/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<DistrictResponse>> updateDistrict(
            @PathVariable Long id, @Valid @RequestBody DistrictRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.updateDistrict(id, request), "District updated", ApiPaths.DISTRICTS + "/" + id));
    }

    /** PATCH /api/v1/districts/{id}/toggle — Toggle status. Accessible by: SUPER_ADMIN */
    @PatchMapping(ApiPaths.DISTRICTS + "/{id}/toggle")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleDistrict(@PathVariable Long id) {
        geographyService.toggleDistrictStatus(id);
        return ResponseEntity.ok(ApiResponse.success(null, "District status toggled", ApiPaths.DISTRICTS + "/" + id + "/toggle"));
    }

    // ==================== BLOCKS ====================

    /** POST /api/v1/blocks — Create block. Accessible by: SUPER_ADMIN, SYSTEM_ADMIN */
    @PostMapping(ApiPaths.BLOCKS)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<BlockResponse>> createBlock(
            @Valid @RequestBody BlockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                geographyService.createBlock(request), "Block created successfully", ApiPaths.BLOCKS));
    }

    /** GET /api/v1/blocks/{id} — Get block by ID. Accessible by: All authenticated */
    @GetMapping(ApiPaths.BLOCKS + "/{id}")
    public ResponseEntity<ApiResponse<BlockResponse>> getBlock(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.getBlockById(id), ApiPaths.BLOCKS + "/" + id));
    }

    /** GET /api/v1/blocks?districtId=1 — Get blocks by district. Accessible by: All */
    @GetMapping(ApiPaths.BLOCKS)
    public ResponseEntity<ApiResponse<PageResponse<BlockResponse>>> getBlocksByDistrict(
            @RequestParam Long districtId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.getBlocksByDistrict(districtId, page, size), ApiPaths.BLOCKS));
    }

    /** GET /api/v1/blocks/active?districtId=1 — Active blocks for dropdown. Accessible by: All */
    @GetMapping(ApiPaths.BLOCKS + "/active")
    public ResponseEntity<ApiResponse<List<BlockResponse>>> getActiveBlocks(
            @RequestParam Long districtId) {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.getActiveBlocksByDistrict(districtId), ApiPaths.BLOCKS + "/active"));
    }

    /** PUT /api/v1/blocks/{id} — Update block. Accessible by: SUPER_ADMIN, SYSTEM_ADMIN */
    @PutMapping(ApiPaths.BLOCKS + "/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<BlockResponse>> updateBlock(
            @PathVariable Long id, @Valid @RequestBody BlockRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.updateBlock(id, request), "Block updated", ApiPaths.BLOCKS + "/" + id));
    }

    /** PATCH /api/v1/blocks/{id}/toggle — Toggle status. Accessible by: SUPER_ADMIN */
    @PatchMapping(ApiPaths.BLOCKS + "/{id}/toggle")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleBlock(@PathVariable Long id) {
        geographyService.toggleBlockStatus(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Block status toggled", ApiPaths.BLOCKS + "/" + id + "/toggle"));
    }

    // ==================== STORES ====================

    /** POST /api/v1/stores — Create store. Accessible by: SUPER_ADMIN, SYSTEM_ADMIN */
    @PostMapping(ApiPaths.STORES)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(
            @Valid @RequestBody StoreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                geographyService.createStore(request), "Store created successfully", ApiPaths.STORES));
    }

    /** GET /api/v1/stores/{id} — Get store by ID. Accessible by: All authenticated */
    @GetMapping(ApiPaths.STORES + "/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStore(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.getStoreById(id), ApiPaths.STORES + "/" + id));
    }

    /** GET /api/v1/stores?blockId=1 — Get stores by block. Accessible by: All */
    @GetMapping(ApiPaths.STORES)
    public ResponseEntity<ApiResponse<PageResponse<StoreResponse>>> getStoresByBlock(
            @RequestParam Long blockId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.getStoresByBlock(blockId, page, size), ApiPaths.STORES));
    }

    /** PUT /api/v1/stores/{id} — Update store. Accessible by: SUPER_ADMIN, SYSTEM_ADMIN */
    @PutMapping(ApiPaths.STORES + "/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @PathVariable Long id, @Valid @RequestBody StoreRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                geographyService.updateStore(id, request), "Store updated", ApiPaths.STORES + "/" + id));
    }

    /** PATCH /api/v1/stores/{id}/toggle — Toggle status. Accessible by: SUPER_ADMIN */
    @PatchMapping(ApiPaths.STORES + "/{id}/toggle")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleStore(@PathVariable Long id) {
        geographyService.toggleStoreStatus(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Store status toggled", ApiPaths.STORES + "/" + id + "/toggle"));
    }
}
