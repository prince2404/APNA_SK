package com.ask.service;

import com.ask.dto.request.inventory.*;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.inventory.*;

import java.util.List;

/**
 * Service interface for managing central stock warehouse, transfer orders,
 * store inventory levels, stock requests, and adjustments.
 */
public interface InventoryService {

    // Central stock receipt
    StockCentralResponse receiveCentralStock(StockReceiptRequest request, String currentUserEmail);
    PageResponse<StockCentralResponse> getCentralStock(int page, int size);

    // Transfer orders
    TransferOrderResponse createTransferOrder(TransferOrderRequest request, String currentUserEmail);
    PageResponse<TransferOrderResponse> getTransferOrders(String currentUserEmail, int page, int size);
    TransferOrderResponse getTransferOrderById(Long id, String currentUserEmail);
    TransferOrderResponse confirmTransferReceipt(Long id, String currentUserEmail);
    TransferOrderResponse cancelTransferOrder(Long id, String currentUserEmail);

    // Store stock
    PageResponse<StockStoreResponse> getStoreStock(String currentUserEmail, Long storeId, String search, Long categoryId, int page, int size);
    List<StockStoreResponse> getLowStockAlerts(String currentUserEmail, Long storeId);
    List<StockStoreResponse> getExpiringStock(String currentUserEmail, Long storeId, int days);

    // Stock adjustments
    StockAdjustmentResponse adjustStock(StockAdjustmentRequest request, String currentUserEmail);
    PageResponse<StockAdjustmentResponse> getStockAdjustments(String currentUserEmail, Long storeId, int page, int size);

    // Stock requests
    StockRequestResponse createStockRequest(StockRequestRequest request, String currentUserEmail);
    PageResponse<StockRequestResponse> getStockRequests(String currentUserEmail, Long storeId, String status, int page, int size);
    StockRequestResponse reviewStockRequest(Long id, StockRequestReviewRequest request, String currentUserEmail);
}
