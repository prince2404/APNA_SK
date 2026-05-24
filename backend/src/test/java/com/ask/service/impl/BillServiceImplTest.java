package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.request.billing.BillItemRequest;
import com.ask.dto.request.billing.BillRequest;
import com.ask.dto.response.billing.BillResponse;
import com.ask.entity.*;
import com.ask.enums.*;
import com.ask.exception.BusinessRuleException;
import com.ask.mapper.BillMapper;
import com.ask.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @Mock
    private BillRepository billRepository;
    @Mock
    private BillItemRepository billItemRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private HealthCardRepository healthCardRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockStoreRepository stockStoreRepository;
    @Mock
    private SchemeRepository schemeRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private CommissionConfigRepository commissionConfigRepository;
    @Mock
    private CommissionEntryRepository commissionEntryRepository;
    @Mock
    private SystemConfigRepository systemConfigRepository;
    @Mock
    private BillMapper billMapper;

    @InjectMocks
    private BillServiceImpl billService;

    private User receptionist;
    private Store store;
    private Block block;
    private District district;
    private State state;
    private Patient patient;
    private Product product;
    private StockStore stockStore;
    private Role blockAdminRole;
    private User blockAdminUser;
    private CommissionConfig blockCommConfig;

    @BeforeEach
    void setUp() {
        state = State.builder().id(1L).name("Bihar").status(EntityStatus.ACTIVE).build();
        district = District.builder().id(1L).name("Patna").state(state).status(EntityStatus.ACTIVE).build();
        block = Block.builder().id(1L).name("Block A").district(district).status(EntityStatus.ACTIVE).build();
        store = Store.builder().id(1L).name("Store A").code("ST01").block(block).status(EntityStatus.ACTIVE).build();

        Role receptionistRole = Role.builder().id(6L).name(RoleConstants.RECEPTIONIST).build();
        receptionist = User.builder().id(1L).email("rec@ask.com").role(receptionistRole).store(store).build();

        patient = Patient.builder().id(1L).fullName("John Patient").state(state).district(district).block(block).store(store).status(EntityStatus.ACTIVE).build();

        ProductCategory category = ProductCategory.builder().id(1L).name("Medicines").status(EntityStatus.ACTIVE).build();
        product = Product.builder()
                .id(1L)
                .name("Paracetamol")
                .brand("Brand X")
                .category(category)
                .mrp(BigDecimal.valueOf(10.00))
                .askPrice(BigDecimal.valueOf(8.00))
                .gstPercentage(BigDecimal.valueOf(12.00))
                .status(EntityStatus.ACTIVE)
                .build();

        stockStore = StockStore.builder()
                .id(1L)
                .store(store)
                .product(product)
                .batchNumber("B123")
                .quantity(100)
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        blockAdminRole = Role.builder().id(5L).name(RoleConstants.BLOCK_ADMIN).build();
        blockAdminUser = User.builder().id(2L).email("block@ask.com").role(blockAdminRole).block(block).status(UserStatus.ACTIVE).build();

        blockCommConfig = CommissionConfig.builder()
                .id(1L)
                .role(blockAdminRole)
                .percentage(BigDecimal.valueOf(5.00))
                .build();
    }

    @Test
    void createBill_Success() {
        when(userRepository.findByEmail("rec@ask.com")).thenReturn(Optional.of(receptionist));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(healthCardRepository.findByPatientId(1L)).thenReturn(Optional.empty());
        when(billRepository.countByStoreIdAndCreatedAtAfter(anyLong(), any(LocalDateTime.class))).thenReturn(0L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(stockStoreRepository.findByStoreIdAndProductIdAndBatchNumber(1L, 1L, "B123")).thenReturn(Optional.of(stockStore));
        when(schemeRepository.findActiveSchemesAtDate(any(EntityStatus.class), any(LocalDate.now().getClass()))).thenReturn(Collections.emptyList());

        // Commission resolution mocks
        when(roleRepository.findByName(anyString())).thenAnswer(invocation -> {
            String roleName = invocation.getArgument(0);
            return Optional.of(Role.builder().id(roleName.hashCode() * 1L).name(roleName).build());
        });
        when(commissionConfigRepository.findByRoleId(anyLong())).thenReturn(Optional.of(CommissionConfig.builder().percentage(BigDecimal.valueOf(2.00)).build()));
        when(userRepository.findAll(any(Specification.class))).thenReturn(Collections.singletonList(blockAdminUser));

        BillRequest request = BillRequest.builder()
                .patientId(1L)
                .paymentMode(PaymentMode.CASH)
                .items(Collections.singletonList(BillItemRequest.builder()
                        .productId(1L)
                        .batchNumber("B123")
                        .quantity(5)
                        .build()))
                .build();

        when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(billMapper.toBillResponse(any(Bill.class))).thenReturn(BillResponse.builder().billNumber("ASK-ST01-YYYYMMDD-0001").netAmount(BigDecimal.valueOf(40.00)).build());

        BillResponse response = billService.createBill(request, "rec@ask.com");

        assertNotNull(response);
        assertEquals(95, stockStore.getQuantity()); // stock deducted: 100 - 5 = 95
        verify(billRepository, times(1)).save(any(Bill.class));
        verify(commissionEntryRepository, atLeastOnce()).save(any(CommissionEntry.class));
    }

    @Test
    void createBill_InsufficientStock_ThrowsException() {
        when(userRepository.findByEmail("rec@ask.com")).thenReturn(Optional.of(receptionist));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        stockStore.setQuantity(2);
        when(stockStoreRepository.findByStoreIdAndProductIdAndBatchNumber(1L, 1L, "B123")).thenReturn(Optional.of(stockStore));

        BillRequest request = BillRequest.builder()
                .patientId(1L)
                .paymentMode(PaymentMode.CASH)
                .items(Collections.singletonList(BillItemRequest.builder()
                        .productId(1L)
                        .batchNumber("B123")
                        .quantity(5) // requests 5, but stock is 2
                        .build()))
                .build();

        assertThrows(BusinessRuleException.class, () -> billService.createBill(request, "rec@ask.com"));
    }
}
