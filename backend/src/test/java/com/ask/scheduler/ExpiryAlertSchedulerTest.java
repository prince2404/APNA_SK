package com.ask.scheduler;

import com.ask.entity.*;
import com.ask.enums.NotificationType;
import com.ask.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpiryAlertSchedulerTest {

    @Mock
    private EntityManager entityManager;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ExpiryAlertScheduler scheduler;

    @Test
    void runExpiryScanGeneratesNotificationsForStoreAndCentralExpiringStocks() {
        // Mock query for StockStore
        TypedQuery<StockStore> storeQueryMock = mock(TypedQuery.class);
        when(entityManager.createQuery(contains("StockStore"), eq(StockStore.class))).thenReturn(storeQueryMock);
        when(storeQueryMock.setParameter(anyString(), any())).thenReturn(storeQueryMock);

        Product prod = Product.builder().name("Amlodipine").build();
        Store store = Store.builder().id(5L).name("Store Patna").build();
        StockStore ss = StockStore.builder()
                .id(1L)
                .product(prod)
                .store(store)
                .batchNumber("B123")
                .expiryDate(LocalDate.now().plusDays(10))
                .quantity(10)
                .build();
        when(storeQueryMock.getResultList()).thenReturn(List.of(ss));

        // Mock staff lookup query
        TypedQuery<User> staffQueryMock = mock(TypedQuery.class);
        when(entityManager.createQuery(contains("User u WHERE u.status = 'ACTIVE' AND u.store.id"), eq(User.class))).thenReturn(staffQueryMock);
        when(staffQueryMock.setParameter(eq("storeId"), eq(5L))).thenReturn(staffQueryMock);
        User staff = User.builder().id(9L).email("staff@askhealth.in").build();
        when(staffQueryMock.getResultList()).thenReturn(List.of(staff));

        // Mock query for StockCentral
        TypedQuery<StockCentral> centralQueryMock = mock(TypedQuery.class);
        when(entityManager.createQuery(contains("StockCentral"), eq(StockCentral.class))).thenReturn(centralQueryMock);
        when(centralQueryMock.setParameter(anyString(), any())).thenReturn(centralQueryMock);
        StockCentral sc = StockCentral.builder()
                .id(2L)
                .product(prod)
                .batchNumber("B999")
                .expiryDate(LocalDate.now().plusDays(15))
                .quantity(150)
                .build();
        when(centralQueryMock.getResultList()).thenReturn(List.of(sc));

        // Mock central lookup query
        TypedQuery<User> pharmQueryMock = mock(TypedQuery.class);
        when(entityManager.createQuery(contains("User u WHERE u.status = 'ACTIVE' AND u.role.name"), eq(User.class))).thenReturn(pharmQueryMock);
        when(pharmQueryMock.setParameter(eq("roleName"), eq("PHARMACIST"))).thenReturn(pharmQueryMock);
        User pharm = User.builder().id(11L).email("pharmacist@askhealth.in").build();
        when(pharmQueryMock.getResultList()).thenReturn(List.of(pharm));

        scheduler.runExpiryScan();

        verify(notificationService).sendNotification(
                eq(staff),
                eq(NotificationType.EXPIRY_ALERT),
                eq("Store Stock Expiry Warning"),
                contains("B123"),
                eq("STOCK_STORE"),
                eq(1L)
        );

        verify(notificationService).sendNotification(
                eq(pharm),
                eq(NotificationType.EXPIRY_ALERT),
                eq("Central Warehouse Expiry Alert"),
                contains("B999"),
                eq("STOCK_CENTRAL"),
                eq(2L)
        );
    }
}
