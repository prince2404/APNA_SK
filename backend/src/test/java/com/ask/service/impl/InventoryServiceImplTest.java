package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.request.inventory.*;
import com.ask.dto.response.inventory.*;
import com.ask.entity.*;
import com.ask.enums.*;
import org.springframework.security.access.AccessDeniedException;
import com.ask.exception.BusinessRuleException;
import com.ask.mapper.InventoryMapper;
import com.ask.mapper.StockRequestMapper;
import com.ask.repository.*;
import com.ask.service.AuditService;
import com.ask.service.EmailService;
import com.ask.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StockCentralRepository stockCentralRepository;
    @Mock
    private TransferOrderRepository transferOrderRepository;
    @Mock
    private StockStoreRepository stockStoreRepository;
    @Mock
    private StockAdjustmentRepository stockAdjustmentRepository;
    @Mock
    private StockRequestRepository stockRequestRepository;

    @Mock
    private InventoryMapper inventoryMapper;
    @Mock
    private StockRequestMapper stockRequestMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AuditService auditService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private User pharmacist;
    private User receptionist;
    private Store store;
    private Product product;
    private StockCentral centralStock;

    @BeforeEach
    void setUp() {
        Role pharmRole = Role.builder().name(RoleConstants.PHARMACIST).hierarchyLevel(3).build();
        Role recepRole = Role.builder().name(RoleConstants.RECEPTIONIST).hierarchyLevel(6).build();

        store = Store.builder().id(1L).name("SK Patna").build();

        pharmacist = User.builder().id(1L).email("pharm@ask.in").role(pharmRole).build();
        receptionist = User.builder().id(2L).email("recep@ask.in").role(recepRole).store(store).build();

        product = Product.builder()
                .id(1L)
                .name("Aspirin")
                .minStockThreshold(10)
                .status(EntityStatus.ACTIVE)
                .mrp(new BigDecimal("10.00"))
                .askPrice(new BigDecimal("8.00"))
                .build();

        centralStock = StockCentral.builder()
                .id(1L)
                .product(product)
                .batchNumber("B123")
                .quantity(100)
                .expiryDate(LocalDate.now().plusDays(200))
                .status(StockCentralStatus.AVAILABLE)
                .build();
    }

    @Test
    void receiveCentralStockSucceedsForPharmacist() {
        StockReceiptRequest req = StockReceiptRequest.builder()
                .productId(1L)
                .batchNumber("B123")
                .quantity(50)
                .expiryDate(LocalDate.now().plusDays(100))
                .build();

        when(userRepository.findByEmail(pharmacist.getEmail())).thenReturn(Optional.of(pharmacist));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(stockCentralRepository.findByProductIdAndBatchNumberAndStatus(1L, "B123", StockCentralStatus.AVAILABLE))
                .thenReturn(Optional.empty());
        when(stockCentralRepository.save(any(StockCentral.class))).thenReturn(centralStock);
        when(inventoryMapper.toStockCentralResponse(any(StockCentral.class)))
                .thenReturn(StockCentralResponse.builder().id(1L).quantity(50).build());

        StockCentralResponse resp = inventoryService.receiveCentralStock(req, pharmacist.getEmail());

        assertNotNull(resp);
        verify(stockCentralRepository, times(1)).save(any(StockCentral.class));
        verify(auditService, times(1)).log(any(), anyString(), anyString(), any(), any(), any(), any(), anyString());
    }

    @Test
    void receiveCentralStockThrowsForReceptionist() {
        StockReceiptRequest req = StockReceiptRequest.builder().productId(1L).build();
        when(userRepository.findByEmail(receptionist.getEmail())).thenReturn(Optional.of(receptionist));

        assertThrows(AccessDeniedException.class, () -> {
            inventoryService.receiveCentralStock(req, receptionist.getEmail());
        });
    }

    @Test
    void createTransferOrderDeductsCentralStock() {
        TransferOrderRequest req = TransferOrderRequest.builder()
                .storeId(1L)
                .notes("Sending stock")
                .items(List.of(TransferOrderRequest.TransferItemRequest.builder()
                        .productId(1L)
                        .batchNumber("B123")
                        .quantity(40)
                        .build()))
                .build();

        TransferOrder order = TransferOrder.builder()
                .id(1L)
                .transferNumber("TO-123456")
                .store(store)
                .createdBy(pharmacist)
                .status(TransferStatus.PENDING)
                .build();

        when(userRepository.findByEmail(pharmacist.getEmail())).thenReturn(Optional.of(pharmacist));
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(stockCentralRepository.findByProductIdAndBatchNumberAndStatus(1L, "B123", StockCentralStatus.AVAILABLE))
                .thenReturn(Optional.of(centralStock));
        when(transferOrderRepository.save(any(TransferOrder.class))).thenReturn(order);
        when(inventoryMapper.toTransferOrderResponse(any(TransferOrder.class)))
                .thenReturn(TransferOrderResponse.builder().id(1L).transferNumber("TO-123456").status(TransferStatus.PENDING).build());

        TransferOrderResponse resp = inventoryService.createTransferOrder(req, pharmacist.getEmail());

        assertNotNull(resp);
        assertEquals(60, centralStock.getQuantity()); // 100 - 40
        verify(stockCentralRepository, times(1)).save(centralStock);
        verify(transferOrderRepository, times(1)).save(any(TransferOrder.class));
    }

    @Test
    void createTransferOrderThrowsIfInsufficientStock() {
        TransferOrderRequest req = TransferOrderRequest.builder()
                .storeId(1L)
                .items(List.of(TransferOrderRequest.TransferItemRequest.builder()
                        .productId(1L)
                        .batchNumber("B123")
                        .quantity(150) // exceed 100 available
                        .build()))
                .build();

        when(userRepository.findByEmail(pharmacist.getEmail())).thenReturn(Optional.of(pharmacist));
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(stockCentralRepository.findByProductIdAndBatchNumberAndStatus(1L, "B123", StockCentralStatus.AVAILABLE))
                .thenReturn(Optional.of(centralStock));

        assertThrows(BusinessRuleException.class, () -> {
            inventoryService.createTransferOrder(req, pharmacist.getEmail());
        });
    }

    @Test
    void confirmTransferReceiptIncrementsStoreStock() {
        TransferOrder order = TransferOrder.builder()
                .id(1L)
                .transferNumber("TO-123456")
                .store(store)
                .createdBy(pharmacist)
                .status(TransferStatus.PENDING)
                .build();
        
        TransferOrderItem item = TransferOrderItem.builder()
                .transferOrder(order)
                .product(product)
                .batchNumber("B123")
                .expiryDate(LocalDate.now().plusDays(200))
                .quantity(30)
                .build();
        order.setItems(List.of(item));

        when(userRepository.findByEmail(receptionist.getEmail())).thenReturn(Optional.of(receptionist));
        when(transferOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(stockStoreRepository.findByStoreIdAndProductIdAndBatchNumber(1L, 1L, "B123")).thenReturn(Optional.empty());
        when(transferOrderRepository.save(any(TransferOrder.class))).thenReturn(order);
        when(inventoryMapper.toTransferOrderResponse(any(TransferOrder.class)))
                .thenReturn(TransferOrderResponse.builder().status(TransferStatus.RECEIVED).build());

        TransferOrderResponse resp = inventoryService.confirmTransferReceipt(1L, receptionist.getEmail());

        assertNotNull(resp);
        assertEquals(TransferStatus.RECEIVED, order.getStatus());
        verify(stockStoreRepository, times(1)).save(any(StockStore.class));
    }

    @Test
    void adjustStockTriggersLowStockNotification() {
        StockAdjustmentRequest req = StockAdjustmentRequest.builder()
                .productId(1L)
                .batchNumber("B123")
                .adjustmentType(AdjustmentType.DAMAGE)
                .quantityChange(-15) // drops stock below threshold
                .build();

        StockStore storeStock = StockStore.builder()
                .store(store)
                .product(product)
                .batchNumber("B123")
                .quantity(20) // will become 5, below threshold 10
                .build();

        StockAdjustment adj = StockAdjustment.builder().id(1L).build();

        when(userRepository.findByEmail(receptionist.getEmail())).thenReturn(Optional.of(receptionist));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(stockStoreRepository.findByStoreIdAndProductIdAndBatchNumber(1L, 1L, "B123")).thenReturn(Optional.of(storeStock));
        when(stockStoreRepository.save(storeStock)).thenReturn(storeStock);
        when(stockAdjustmentRepository.save(any(StockAdjustment.class))).thenReturn(adj);
        when(userRepository.findByRoleNameIn(anyList())).thenReturn(List.of(pharmacist));

        inventoryService.adjustStock(req, receptionist.getEmail());

        assertEquals(5, storeStock.getQuantity());
        verify(notificationService, atLeastOnce()).sendNotification(any(), eq(NotificationType.STOCK_ALERT), anyString(), anyString(), anyString(), any());
    }
}
