package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.inventory.*;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.inventory.*;
import com.ask.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing central warehouse receipt, stock transfers, store stocks,
 * stock adjustments, and store replenishment requests.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // ==================== CENTRAL WAREHOUSE STOCK ====================

    @PostMapping(ApiPaths.STOCK_CENTRAL + "/receipt")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN') or hasRole('PHARMACIST')")
    public ResponseEntity<ApiResponse<StockCentralResponse>> receiveCentralStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StockReceiptRequest request) {
        StockCentralResponse response = inventoryService.receiveCentralStock(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Stock received at central warehouse", ApiPaths.STOCK_CENTRAL + "/receipt"));
    }

    @GetMapping(ApiPaths.STOCK_CENTRAL)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN') or hasRole('PHARMACIST')")
    public ResponseEntity<ApiResponse<PageResponse<StockCentralResponse>>> getCentralStock(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<StockCentralResponse> response = inventoryService.getCentralStock(page, size);
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.STOCK_CENTRAL));
    }

    // ==================== TRANSFER ORDERS ====================

    @PostMapping(ApiPaths.TRANSFER_ORDERS)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN') or hasRole('PHARMACIST')")
    public ResponseEntity<ApiResponse<TransferOrderResponse>> createTransferOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransferOrderRequest request) {
        TransferOrderResponse response = inventoryService.createTransferOrder(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Stock transfer order created successfully", ApiPaths.TRANSFER_ORDERS));
    }

    @GetMapping(ApiPaths.TRANSFER_ORDERS)
    public ResponseEntity<ApiResponse<PageResponse<TransferOrderResponse>>> getTransferOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<TransferOrderResponse> response = inventoryService.getTransferOrders(userDetails.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.TRANSFER_ORDERS));
    }

    @GetMapping(ApiPaths.TRANSFER_ORDERS + "/{id}")
    public ResponseEntity<ApiResponse<TransferOrderResponse>> getTransferOrderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        TransferOrderResponse response = inventoryService.getTransferOrderById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.TRANSFER_ORDERS + "/" + id));
    }

    @PatchMapping(ApiPaths.TRANSFER_ORDERS + "/{id}/confirm")
    public ResponseEntity<ApiResponse<TransferOrderResponse>> confirmTransferReceipt(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        TransferOrderResponse response = inventoryService.confirmTransferReceipt(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Transfer order confirmed as received", ApiPaths.TRANSFER_ORDERS + "/" + id + "/confirm"));
    }

    @PatchMapping(ApiPaths.TRANSFER_ORDERS + "/{id}/cancel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN') or hasRole('PHARMACIST')")
    public ResponseEntity<ApiResponse<TransferOrderResponse>> cancelTransferOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        TransferOrderResponse response = inventoryService.cancelTransferOrder(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Transfer order cancelled successfully", ApiPaths.TRANSFER_ORDERS + "/" + id + "/cancel"));
    }

    // ==================== STORE STOCK LEVELS & ALERTS ====================

    @GetMapping(ApiPaths.STOCK_STORE)
    public ResponseEntity<ApiResponse<PageResponse<StockStoreResponse>>> getStoreStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<StockStoreResponse> response = inventoryService.getStoreStock(
                userDetails.getUsername(), storeId, search, categoryId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.STOCK_STORE));
    }

    @GetMapping(ApiPaths.STOCK_STORE + "/alerts/low")
    public ResponseEntity<ApiResponse<List<StockStoreResponse>>> getLowStockAlerts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long storeId) {
        List<StockStoreResponse> response = inventoryService.getLowStockAlerts(userDetails.getUsername(), storeId);
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.STOCK_STORE + "/alerts/low"));
    }

    @GetMapping(ApiPaths.STOCK_STORE + "/alerts/expiry")
    public ResponseEntity<ApiResponse<List<StockStoreResponse>>> getExpiringStockAlerts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "90") int days) {
        List<StockStoreResponse> response = inventoryService.getExpiringStock(userDetails.getUsername(), storeId, days);
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.STOCK_STORE + "/alerts/expiry"));
    }

    // ==================== STOCK ADJUSTMENTS ====================

    @PostMapping(ApiPaths.STOCK_ADJUSTMENTS)
    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('BLOCK_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> adjustStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StockAdjustmentRequest request) {
        StockAdjustmentResponse response = inventoryService.adjustStock(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Stock adjustment logged successfully", ApiPaths.STOCK_ADJUSTMENTS));
    }

    @GetMapping(ApiPaths.STOCK_ADJUSTMENTS)
    public ResponseEntity<ApiResponse<PageResponse<StockAdjustmentResponse>>> getStockAdjustments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<StockAdjustmentResponse> response = inventoryService.getStockAdjustments(userDetails.getUsername(), storeId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.STOCK_ADJUSTMENTS));
    }

    // ==================== STOCK REQUESTS ====================

    @PostMapping(ApiPaths.STOCK_REQUESTS)
    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('BLOCK_ADMIN')")
    public ResponseEntity<ApiResponse<StockRequestResponse>> createStockRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StockRequestRequest request) {
        StockRequestResponse response = inventoryService.createStockRequest(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Stock request submitted successfully", ApiPaths.STOCK_REQUESTS));
    }

    @GetMapping(ApiPaths.STOCK_REQUESTS)
    public ResponseEntity<ApiResponse<PageResponse<StockRequestResponse>>> getStockRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<StockRequestResponse> response = inventoryService.getStockRequests(userDetails.getUsername(), storeId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.STOCK_REQUESTS));
    }

    @PatchMapping(ApiPaths.STOCK_REQUESTS + "/{id}/review")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN') or hasRole('PHARMACIST')")
    public ResponseEntity<ApiResponse<StockRequestResponse>> reviewStockRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody StockRequestReviewRequest request) {
        StockRequestResponse response = inventoryService.reviewStockRequest(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Stock request reviewed successfully", ApiPaths.STOCK_REQUESTS + "/" + id + "/review"));
    }
}
