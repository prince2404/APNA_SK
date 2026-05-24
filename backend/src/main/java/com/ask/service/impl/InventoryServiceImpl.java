package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.request.inventory.*;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.inventory.*;
import com.ask.entity.*;
import com.ask.enums.*;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.GeographicScopeException;
import com.ask.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.ask.mapper.InventoryMapper;
import com.ask.mapper.StockRequestMapper;
import com.ask.repository.*;
import com.ask.service.AuditService;
import com.ask.service.EmailService;
import com.ask.service.InventoryService;
import com.ask.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of InventoryService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StockCentralRepository stockCentralRepository;
    private final TransferOrderRepository transferOrderRepository;
    private final TransferOrderItemRepository transferOrderItemRepository;
    private final StockStoreRepository stockStoreRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final StockRequestRepository stockRequestRepository;

    private final InventoryMapper inventoryMapper;
    private final StockRequestMapper stockRequestMapper;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final EmailService emailService;

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void ensurePharmacistOrAdmin(User user) {
        String role = user.getRole().getName();
        if (!role.equals(RoleConstants.PHARMACIST)
                && !role.equals(RoleConstants.SUPER_ADMIN)
                && !role.equals(RoleConstants.SYSTEM_ADMIN)) {
            throw new AccessDeniedException("Access denied. Pharmacist or Admin role required.");
        }
    }

    private boolean hasPlatformScope(User user) {
        String roleName = user.getRole().getName();
        return RoleConstants.SUPER_ADMIN.equals(roleName) || RoleConstants.SYSTEM_ADMIN.equals(roleName);
    }

    private void ensureStoreVisible(User currentUser, Store store) {
        if (hasPlatformScope(currentUser)) {
            return;
        }
        State state = (store.getBlock() != null && store.getBlock().getDistrict() != null) ? store.getBlock().getDistrict().getState() : null;
        District district = (store.getBlock() != null) ? store.getBlock().getDistrict() : null;
        Block block = store.getBlock();
        if (!isGeographyWithinScope(currentUser, state, district, block, store)) {
            throw new GeographicScopeException();
        }
    }

    private boolean isGeographyWithinScope(User currentUser, State state, District district, Block block, Store store) {
        if (currentUser.getStore() != null) {
            return store != null && currentUser.getStore().getId().equals(store.getId());
        }
        if (currentUser.getBlock() != null) {
            return block != null && currentUser.getBlock().getId().equals(block.getId());
        }
        if (currentUser.getDistrict() != null) {
            return district != null && currentUser.getDistrict().getId().equals(district.getId());
        }
        if (currentUser.getState() != null) {
            return state != null && currentUser.getState().getId().equals(state.getId());
        }
        return false;
    }

    @Override
    @Transactional
    public StockCentralResponse receiveCentralStock(StockReceiptRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensurePharmacistOrAdmin(currentUser);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (request.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Cannot receive expired stock");
        }

        // Check if an AVAILABLE record for product and batch exists, increment it. Otherwise create new.
        Optional<StockCentral> existingOpt = stockCentralRepository.findByProductIdAndBatchNumberAndStatus(
                product.getId(), request.getBatchNumber(), StockCentralStatus.AVAILABLE);

        StockCentral saved;
        if (existingOpt.isPresent()) {
            StockCentral existing = existingOpt.get();
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            saved = stockCentralRepository.save(existing);
        } else {
            StockCentral central = StockCentral.builder()
                    .product(product)
                    .batchNumber(request.getBatchNumber())
                    .manufacturingDate(request.getManufacturingDate())
                    .expiryDate(request.getExpiryDate())
                    .quantity(request.getQuantity())
                    .receivedBy(currentUser)
                    .status(StockCentralStatus.AVAILABLE)
                    .build();
            saved = stockCentralRepository.save(central);
        }

        auditService.log(currentUser, "RECEIVE_CENTRAL_STOCK", "STOCK_CENTRAL", saved.getId(), null, null, null,
                "Received quantity: " + request.getQuantity() + " for product: " + product.getName() + " (batch: " + request.getBatchNumber() + ")");

        return inventoryMapper.toStockCentralResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockCentralResponse> getCentralStock(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "receivedAt"));
        Page<StockCentral> centralPage = stockCentralRepository.findAll(pageable);
        List<StockCentralResponse> content = centralPage.getContent().stream()
                .map(inventoryMapper::toStockCentralResponse)
                .collect(Collectors.toList());
        return PageResponse.of(centralPage, content);
    }

    @Override
    @Transactional
    public TransferOrderResponse createTransferOrder(TransferOrderRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensurePharmacistOrAdmin(currentUser);

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", request.getStoreId()));

        String transferNumber = "TO-" + System.currentTimeMillis();

        TransferOrder order = TransferOrder.builder()
                .transferNumber(transferNumber)
                .store(store)
                .createdBy(currentUser)
                .status(TransferStatus.PENDING)
                .notes(request.getNotes())
                .items(new ArrayList<>())
                .build();

        for (TransferOrderRequest.TransferItemRequest itemReq : request.getItems()) {
            StockCentral centralStock = stockCentralRepository.findByProductIdAndBatchNumberAndStatus(
                    itemReq.getProductId(), itemReq.getBatchNumber(), StockCentralStatus.AVAILABLE)
                    .orElseThrow(() -> new BusinessRuleException("No available central stock for product ID " + itemReq.getProductId() + " and batch: " + itemReq.getBatchNumber()));

            if (centralStock.getQuantity() < itemReq.getQuantity()) {
                throw new BusinessRuleException("Insufficient quantity for product ID " + itemReq.getProductId() + " and batch: " + itemReq.getBatchNumber() + ". Available: " + centralStock.getQuantity());
            }

            // Deduct central stock
            centralStock.setQuantity(centralStock.getQuantity() - itemReq.getQuantity());
            if (centralStock.getQuantity() == 0) {
                centralStock.setStatus(StockCentralStatus.TRANSFERRED);
            }
            stockCentralRepository.save(centralStock);

            TransferOrderItem item = TransferOrderItem.builder()
                    .transferOrder(order)
                    .product(centralStock.getProduct())
                    .batchNumber(itemReq.getBatchNumber())
                    .expiryDate(centralStock.getExpiryDate())
                    .quantity(itemReq.getQuantity())
                    .build();

            order.getItems().add(item);
        }

        TransferOrder saved = transferOrderRepository.save(order);

        // Notify store staff
        List<User> storeStaff = userRepository.findAll().stream()
                .filter(u -> u.getStore() != null && u.getStore().getId().equals(store.getId()))
                .collect(Collectors.toList());

        for (User staff : storeStaff) {
            try {
                notificationService.sendNotification(
                        staff,
                        NotificationType.STOCK_TRANSFER,
                        "New Stock Transfer Pending",
                        "Stock transfer order " + saved.getTransferNumber() + " has been dispatched for store " + store.getName() + ".",
                        "TRANSFER_ORDER",
                        saved.getId()
                );
            } catch (Exception e) {
                log.error("Failed to notify store staff: {}", staff.getEmail(), e);
            }
        }

        auditService.log(currentUser, "CREATE_TRANSFER_ORDER", "TRANSFER_ORDER", saved.getId(), null, null, null,
                "Created transfer order: " + saved.getTransferNumber() + " to store: " + store.getName());

        return inventoryMapper.toTransferOrderResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransferOrderResponse> getTransferOrders(String currentUserEmail, int page, int size) {
        User currentUser = getCurrentUser(currentUserEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TransferOrder> orderPage;

        if (currentUser.getStore() != null) {
            orderPage = transferOrderRepository.findByStoreId(currentUser.getStore().getId(), pageable);
        } else {
            orderPage = transferOrderRepository.findAll(pageable);
        }

        List<TransferOrderResponse> content = orderPage.getContent().stream()
                .map(inventoryMapper::toTransferOrderResponse)
                .collect(Collectors.toList());

        return PageResponse.of(orderPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferOrderResponse getTransferOrderById(Long id, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        TransferOrder order = transferOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferOrder", "id", id));
        ensureStoreVisible(currentUser, order.getStore());
        return inventoryMapper.toTransferOrderResponse(order);
    }

    @Override
    @Transactional
    public TransferOrderResponse confirmTransferReceipt(Long id, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        TransferOrder order = transferOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferOrder", "id", id));

        ensureStoreVisible(currentUser, order.getStore());

        if (order.getStatus() != TransferStatus.PENDING && order.getStatus() != TransferStatus.IN_TRANSIT) {
            throw new BusinessRuleException("Transfer order is not in PENDING or IN_TRANSIT status");
        }

        order.setStatus(TransferStatus.RECEIVED);
        order.setConfirmedBy(currentUser);
        order.setConfirmedAt(LocalDateTime.now());

        // Increment Store Stock
        for (TransferOrderItem item : order.getItems()) {
            Optional<StockStore> stockStoreOpt = stockStoreRepository.findByStoreIdAndProductIdAndBatchNumber(
                    order.getStore().getId(), item.getProduct().getId(), item.getBatchNumber());

            if (stockStoreOpt.isPresent()) {
                StockStore stockStore = stockStoreOpt.get();
                stockStore.setQuantity(stockStore.getQuantity() + item.getQuantity());
                stockStoreRepository.save(stockStore);
            } else {
                StockStore stockStore = StockStore.builder()
                        .store(order.getStore())
                        .product(item.getProduct())
                        .batchNumber(item.getBatchNumber())
                        .expiryDate(item.getExpiryDate())
                        .quantity(item.getQuantity())
                        .build();
                stockStoreRepository.save(stockStore);
            }
        }

        TransferOrder saved = transferOrderRepository.save(order);

        // Notify pharmacist creator
        try {
            notificationService.sendNotification(
                    order.getCreatedBy(),
                    NotificationType.STOCK_TRANSFER,
                    "Transfer Order Received",
                    "Transfer order " + order.getTransferNumber() + " has been confirmed and received at store " + order.getStore().getName() + ".",
                    "TRANSFER_ORDER",
                    saved.getId()
            );
        } catch (Exception e) {
            log.error("Failed to notify pharmacist: {}", order.getCreatedBy().getEmail(), e);
        }

        auditService.log(currentUser, "CONFIRM_TRANSFER_RECEIPT", "TRANSFER_ORDER", saved.getId(), null, null, null,
                "Confirmed receipt for transfer order: " + order.getTransferNumber());

        return inventoryMapper.toTransferOrderResponse(saved);
    }

    @Override
    @Transactional
    public TransferOrderResponse cancelTransferOrder(Long id, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        TransferOrder order = transferOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferOrder", "id", id));

        if (order.getStatus() != TransferStatus.PENDING) {
            throw new BusinessRuleException("Only pending transfer orders can be cancelled");
        }

        order.setStatus(TransferStatus.CANCELLED);
        
        // Restore central stock
        for (TransferOrderItem item : order.getItems()) {
            Optional<StockCentral> stockOpt = stockCentralRepository.findByProductIdAndBatchNumberAndStatus(
                    item.getProduct().getId(), item.getBatchNumber(), StockCentralStatus.AVAILABLE);

            if (stockOpt.isPresent()) {
                StockCentral stock = stockOpt.get();
                stock.setQuantity(stock.getQuantity() + item.getQuantity());
                stockCentralRepository.save(stock);
            } else {
                Optional<StockCentral> transferredOpt = stockCentralRepository.findByProductIdAndBatchNumberAndStatus(
                        item.getProduct().getId(), item.getBatchNumber(), StockCentralStatus.TRANSFERRED);
                if (transferredOpt.isPresent()) {
                    StockCentral stock = transferredOpt.get();
                    stock.setQuantity(item.getQuantity());
                    stock.setStatus(StockCentralStatus.AVAILABLE);
                    stockCentralRepository.save(stock);
                } else {
                    StockCentral newStock = StockCentral.builder()
                            .product(item.getProduct())
                            .batchNumber(item.getBatchNumber())
                            .expiryDate(item.getExpiryDate())
                            .quantity(item.getQuantity())
                            .receivedBy(order.getCreatedBy())
                            .status(StockCentralStatus.AVAILABLE)
                            .build();
                    stockCentralRepository.save(newStock);
                }
            }
        }

        TransferOrder saved = transferOrderRepository.save(order);

        auditService.log(currentUser, "CANCEL_TRANSFER_ORDER", "TRANSFER_ORDER", saved.getId(), null, null, null,
                "Cancelled transfer order: " + order.getTransferNumber());

        return inventoryMapper.toTransferOrderResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockStoreResponse> getStoreStock(String currentUserEmail, Long storeId, String search, Long categoryId, int page, int size) {
        User currentUser = getCurrentUser(currentUserEmail);
        
        Long targetStoreId = storeId;
        if (currentUser.getStore() != null) {
            targetStoreId = currentUser.getStore().getId();
        } else if (targetStoreId == null) {
            throw new BusinessRuleException("Store ID is required");
        }

        final Long finalStoreId = targetStoreId;
        Store store = storeRepository.findById(finalStoreId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", finalStoreId));
        ensureStoreVisible(currentUser, store);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "product.name"));
        Page<StockStore> stockPage;

        boolean hasSearch = search != null && !search.isBlank();
        boolean hasCategory = categoryId != null;

        if (hasSearch && hasCategory) {
            stockPage = stockStoreRepository.findByStoreIdAndProductNameContainingIgnoreCaseAndProductCategoryId(finalStoreId, search, categoryId, pageable);
        } else if (hasSearch) {
            stockPage = stockStoreRepository.findByStoreIdAndProductNameContainingIgnoreCase(finalStoreId, search, pageable);
        } else if (hasCategory) {
            stockPage = stockStoreRepository.findByStoreIdAndProductCategoryId(finalStoreId, categoryId, pageable);
        } else {
            stockPage = stockStoreRepository.findByStoreId(finalStoreId, pageable);
        }

        List<StockStoreResponse> content = stockPage.getContent().stream()
                .map(inventoryMapper::toStockStoreResponse)
                .collect(Collectors.toList());

        return PageResponse.of(stockPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockStoreResponse> getLowStockAlerts(String currentUserEmail, Long storeId) {
        User currentUser = getCurrentUser(currentUserEmail);
        Long targetStoreId = storeId;
        if (currentUser.getStore() != null) {
            targetStoreId = currentUser.getStore().getId();
        }

        List<StockStore> list;
        if (targetStoreId != null) {
            final Long finalStoreId = targetStoreId;
            Store store = storeRepository.findById(finalStoreId)
                    .orElseThrow(() -> new ResourceNotFoundException("Store", "id", finalStoreId));
            ensureStoreVisible(currentUser, store);
            list = stockStoreRepository.findLowStockByStore(finalStoreId);
        } else {
            list = stockStoreRepository.findAllLowStock();
        }

        return list.stream()
                .map(inventoryMapper::toStockStoreResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockStoreResponse> getExpiringStock(String currentUserEmail, Long storeId, int days) {
        User currentUser = getCurrentUser(currentUserEmail);
        LocalDate expiryLimit = LocalDate.now().plusDays(days);
        List<StockStore> list;

        Long targetStoreId = storeId;
        if (currentUser.getStore() != null) {
            targetStoreId = currentUser.getStore().getId();
        }

        if (targetStoreId != null) {
            final Long finalStoreId = targetStoreId;
            Store store = storeRepository.findById(finalStoreId)
                    .orElseThrow(() -> new ResourceNotFoundException("Store", "id", finalStoreId));
            ensureStoreVisible(currentUser, store);
            list = stockStoreRepository.findExpiringStockByStore(finalStoreId, expiryLimit);
        } else {
            list = stockStoreRepository.findExpiringStock(expiryLimit);
        }

        return list.stream()
                .map(inventoryMapper::toStockStoreResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StockAdjustmentResponse adjustStock(StockAdjustmentRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);

        if (currentUser.getStore() == null) {
            throw new BusinessRuleException("Store assignment is required to make adjustments");
        }

        Store store = currentUser.getStore();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        StockStore stockStore = stockStoreRepository.findByStoreIdAndProductIdAndBatchNumber(
                store.getId(), product.getId(), request.getBatchNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Store Stock", "productId", request.getProductId()));

        int newQty = stockStore.getQuantity() + request.getQuantityChange();
        if (newQty < 0) {
            throw new BusinessRuleException("Store stock quantity cannot be negative");
        }

        stockStore.setQuantity(newQty);
        StockStore savedStock = stockStoreRepository.save(stockStore);

        StockAdjustment adjustment = StockAdjustment.builder()
                .store(store)
                .product(product)
                .batchNumber(request.getBatchNumber())
                .adjustmentType(request.getAdjustmentType())
                .quantityChange(request.getQuantityChange())
                .reason(request.getReason())
                .adjustedBy(currentUser)
                .build();

        StockAdjustment savedAdjustment = stockAdjustmentRepository.save(adjustment);

        // Low stock triggers check
        if (newQty < product.getMinStockThreshold()) {
            List<User> alertRecipients = new ArrayList<>(userRepository.findByRoleNameIn(
                    List.of(RoleConstants.SUPER_ADMIN, RoleConstants.SYSTEM_ADMIN, RoleConstants.PHARMACIST)));
            
            // Add block admins
            if (store.getBlock() != null) {
                List<User> blockAdmins = userRepository.findAll().stream()
                        .filter(u -> u.getBlock() != null && u.getBlock().getId().equals(store.getBlock().getId()))
                        .collect(Collectors.toList());
                alertRecipients.addAll(blockAdmins);
            }

            // Add local store staff
            List<User> localStaff = userRepository.findAll().stream()
                    .filter(u -> u.getStore() != null && u.getStore().getId().equals(store.getId()))
                    .collect(Collectors.toList());
            alertRecipients.addAll(localStaff);

            for (User recipient : alertRecipients) {
                try {
                    notificationService.sendNotification(
                            recipient,
                            NotificationType.STOCK_ALERT,
                            "Low Stock Warning: " + product.getName(),
                            "Store " + store.getName() + " is running low on " + product.getName() + " (Current: " + newQty + ", Threshold: " + product.getMinStockThreshold() + ")",
                            "STORE",
                            store.getId()
                    );
                    
                    if (recipient.getEmail() != null && !recipient.getEmail().isBlank()) {
                        emailService.sendSimpleEmail(
                                recipient.getEmail(),
                                "ASK - Low Stock Alert: " + product.getName(),
                                "Dear " + recipient.getFullName() + ",\n\n" +
                                "This is an automated alert that store '" + store.getName() + "' is low on stock for:\n" +
                                "Product: " + product.getName() + "\n" +
                                "Current Quantity: " + newQty + "\n" +
                                "Minimum Threshold: " + product.getMinStockThreshold() + "\n\n" +
                                "Please arrange for stock transfer orders to replenish the store.\n\n" +
                                "Regards,\nApna Swasthya Kendra"
                        );
                    }
                } catch (Exception e) {
                    log.error("Failed to notify alert recipient: {}", recipient.getEmail(), e);
                }
            }
        }

        auditService.log(currentUser, "ADJUST_STORE_STOCK", "STOCK_ADJUSTMENT", savedAdjustment.getId(), null, null, null,
                "Adjusted store stock: " + request.getQuantityChange() + " for product: " + product.getName() + " (type: " + request.getAdjustmentType() + ")");

        return inventoryMapper.toStockAdjustmentResponse(savedAdjustment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockAdjustmentResponse> getStockAdjustments(String currentUserEmail, Long storeId, int page, int size) {
        User currentUser = getCurrentUser(currentUserEmail);
        Long targetStoreId = storeId;
        if (currentUser.getStore() != null) {
            targetStoreId = currentUser.getStore().getId();
        }

        if (targetStoreId == null) {
            throw new BusinessRuleException("Store ID is required");
        }

        final Long finalStoreId = targetStoreId;
        Store store = storeRepository.findById(finalStoreId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", finalStoreId));
        ensureStoreVisible(currentUser, store);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StockAdjustment> adjPage = stockAdjustmentRepository.findByStoreId(finalStoreId, pageable);
        
        List<StockAdjustmentResponse> content = adjPage.getContent().stream()
                .map(inventoryMapper::toStockAdjustmentResponse)
                .collect(Collectors.toList());

        return PageResponse.of(adjPage, content);
    }

    @Override
    @Transactional
    public StockRequestResponse createStockRequest(StockRequestRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);

        if (currentUser.getStore() == null) {
            throw new BusinessRuleException("Store assignment is required to submit stock requests");
        }

        Store store = currentUser.getStore();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        StockRequest stockRequest = StockRequest.builder()
                .store(store)
                .product(product)
                .quantityRequested(request.getQuantityRequested())
                .urgency(request.getUrgency())
                .status(StockRequestStatus.PENDING)
                .requestedBy(currentUser)
                .notes(request.getNotes())
                .build();

        StockRequest saved = stockRequestRepository.save(stockRequest);

        // Notify central pharmacists and admins
        List<User> recipients = userRepository.findByRoleNameIn(List.of(RoleConstants.SUPER_ADMIN, RoleConstants.SYSTEM_ADMIN, RoleConstants.PHARMACIST));
        for (User recipient : recipients) {
            try {
                notificationService.sendNotification(
                        recipient,
                        NotificationType.STOCK_REQUEST,
                        "New Stock Request from " + store.getName(),
                        "User " + currentUser.getFullName() + " requested " + request.getQuantityRequested() + " of product " + product.getName() + " (Urgency: " + request.getUrgency() + ").",
                        "STOCK_REQUEST",
                        saved.getId()
                );
            } catch (Exception e) {
                log.error("Failed to notify: {}", recipient.getEmail(), e);
            }
        }

        auditService.log(currentUser, "CREATE_STOCK_REQUEST", "STOCK_REQUEST", saved.getId(), null, null, null,
                "Requested stock quantity: " + request.getQuantityRequested() + " for product: " + product.getName());

        return stockRequestMapper.toStockRequestResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockRequestResponse> getStockRequests(String currentUserEmail, Long storeId, String status, int page, int size) {
        User currentUser = getCurrentUser(currentUserEmail);
        Long targetStoreId = storeId;
        if (currentUser.getStore() != null) {
            targetStoreId = currentUser.getStore().getId();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StockRequest> reqPage;

        StockRequestStatus reqStatus = null;
        if (status != null && !status.isBlank()) {
            reqStatus = StockRequestStatus.valueOf(status.toUpperCase());
        }

        if (targetStoreId != null) {
            final Long finalStoreId = targetStoreId;
            Store store = storeRepository.findById(finalStoreId)
                    .orElseThrow(() -> new ResourceNotFoundException("Store", "id", finalStoreId));
            ensureStoreVisible(currentUser, store);

            if (reqStatus != null) {
                reqPage = stockRequestRepository.findByStoreIdAndStatus(finalStoreId, reqStatus, pageable);
            } else {
                reqPage = stockRequestRepository.findByStoreId(finalStoreId, pageable);
            }
        } else {
            if (reqStatus != null) {
                reqPage = stockRequestRepository.findByStatus(reqStatus, pageable);
            } else {
                reqPage = stockRequestRepository.findAll(pageable);
            }
        }

        List<StockRequestResponse> content = reqPage.getContent().stream()
                .map(stockRequestMapper::toStockRequestResponse)
                .collect(Collectors.toList());

        return PageResponse.of(reqPage, content);
    }

    @Override
    @Transactional
    public StockRequestResponse reviewStockRequest(Long id, StockRequestReviewRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensurePharmacistOrAdmin(currentUser);

        StockRequest stockRequest = stockRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockRequest", "id", id));

        if (stockRequest.getStatus() != StockRequestStatus.PENDING) {
            throw new BusinessRuleException("Only pending stock requests can be reviewed");
        }

        stockRequest.setStatus(request.getStatus());
        stockRequest.setReviewedBy(currentUser);
        stockRequest.setReviewedAt(LocalDateTime.now());
        stockRequest.setNotes(request.getNotes());

        StockRequest saved = stockRequestRepository.save(stockRequest);

        // Notify the requester
        try {
            notificationService.sendNotification(
                    stockRequest.getRequestedBy(),
                    NotificationType.STOCK_REQUEST,
                    "Stock Request Status Update",
                    "Your stock request for " + stockRequest.getProduct().getName() + " has been " + request.getStatus().toString().toLowerCase() + ".",
                    "STOCK_REQUEST",
                    saved.getId()
            );
        } catch (Exception e) {
            log.error("Failed to notify requester: {}", stockRequest.getRequestedBy().getEmail(), e);
        }

        auditService.log(currentUser, "REVIEW_STOCK_REQUEST", "STOCK_REQUEST", saved.getId(), null, null, null,
                "Reviewed stock request ID " + saved.getId() + " status set to: " + request.getStatus());

        return stockRequestMapper.toStockRequestResponse(saved);
    }
}
