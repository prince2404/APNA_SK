package com.ask.service.impl;

import com.ask.dto.response.dashboard.DashboardResponse;
import com.ask.entity.*;
import com.ask.exception.ResourceNotFoundException;
import com.ask.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User user(Long id, String email, String roleName) {
        Role role = Role.builder().id(id).name(roleName).displayName(roleName).build();
        return User.builder()
                .id(id)
                .fullName("Test User " + id)
                .email(email)
                .role(role)
                .build();
    }

    @Test
    void getDashboardDataReturnsSuperAdminKPIs() {
        User superAdmin = user(1L, "admin@askhealth.in", "SUPER_ADMIN");
        when(userRepository.findByEmail(superAdmin.getEmail())).thenReturn(Optional.of(superAdmin));

        // Mock counts & sums
        TypedQuery<BigDecimal> sumQueryMock = mock(TypedQuery.class);
        when(entityManager.createQuery(contains("SUM(b.netAmount)"), eq(BigDecimal.class))).thenReturn(sumQueryMock);
        when(sumQueryMock.getSingleResult()).thenReturn(BigDecimal.valueOf(50000.00));

        TypedQuery<Long> countQueryMock = mock(TypedQuery.class);
        when(entityManager.createQuery(contains("COUNT"), eq(Long.class))).thenReturn(countQueryMock);
        when(countQueryMock.getSingleResult()).thenReturn(10L);

        // Mock breakdowns & list queries
        TypedQuery<Object[]> trendQueryMock = mock(TypedQuery.class);
        when(entityManager.createQuery(contains("FUNCTION('MONTH', b.billDate)"), eq(Object[].class))).thenReturn(trendQueryMock);
        when(trendQueryMock.setParameter(anyString(), any())).thenReturn(trendQueryMock);
        List<Object[]> trendList = new java.util.ArrayList<>();
        trendList.add(new Object[]{5, BigDecimal.valueOf(10000.00)});
        when(trendQueryMock.getResultList()).thenReturn(trendList);

        TypedQuery<Object[]> geoQueryMock = mock(TypedQuery.class);
        when(entityManager.createQuery(contains("b.store.block.district.state.name"), eq(Object[].class))).thenReturn(geoQueryMock);
        List<Object[]> geoList = new java.util.ArrayList<>();
        geoList.add(new Object[]{"Bihar", BigDecimal.valueOf(25000.00)});
        when(geoQueryMock.getResultList()).thenReturn(geoList);

        TypedQuery<Object[]> productQueryMock = mock(TypedQuery.class);
        when(entityManager.createQuery(contains("item.product.name"), eq(Object[].class))).thenReturn(productQueryMock);
        when(productQueryMock.setMaxResults(anyInt())).thenReturn(productQueryMock);
        List<Object[]> productList = new java.util.ArrayList<>();
        productList.add(new Object[]{"Paracetamol", 100L, BigDecimal.valueOf(500.00)});
        when(productQueryMock.getResultList()).thenReturn(productList);

        TypedQuery<Object[]> activityQueryMock = mock(TypedQuery.class);
        when(entityManager.createQuery(contains("b.billNumber"), eq(Object[].class))).thenReturn(activityQueryMock);
        when(activityQueryMock.setMaxResults(anyInt())).thenReturn(activityQueryMock);
        List<Object[]> activityList = new java.util.ArrayList<>();
        activityList.add(new Object[]{"BILL-001", BigDecimal.valueOf(150.00), LocalDateTime.now(), "Store Patna"});
        when(activityQueryMock.getResultList()).thenReturn(activityList);

        DashboardResponse response = dashboardService.getDashboardData(superAdmin.getEmail());

        assertNotNull(response);
        assertEquals(4, response.getMetrics().size());
        assertEquals("Total Sales", response.getMetrics().get(0).getLabel());
        assertEquals("₹50000.00", response.getMetrics().get(0).getValue());
        assertEquals(1, response.getTrendData().size());
        assertEquals("May", response.getTrendData().get(0).getName());
        assertEquals(10000.00, response.getTrendData().get(0).getValue());
        assertEquals(1, response.getBreakdownData().size());
        assertEquals("Bihar", response.getBreakdownData().get(0).getName());
        assertEquals(25000.00, response.getBreakdownData().get(0).getValue());
        assertEquals(1, response.getTopProducts().size());
        assertEquals("Paracetamol", response.getTopProducts().get(0).getProductName());
        assertEquals(1, response.getRecentActivity().size());
    }

    @Test
    void getDashboardDataThrowsResourceNotFoundIfUserMissing() {
        when(userRepository.findByEmail("missing@askhealth.in")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                dashboardService.getDashboardData("missing@askhealth.in")
        );
    }
}
