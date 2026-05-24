package com.ask.service.impl;

import com.ask.dto.request.commission.CommissionConfigRequest;
import com.ask.dto.response.commission.CommissionConfigResponse;
import com.ask.dto.response.commission.CommissionSummaryResponse;
import com.ask.entity.*;
import com.ask.enums.CommissionStatus;
import com.ask.exception.ResourceNotFoundException;
import com.ask.repository.CommissionConfigRepository;
import com.ask.repository.CommissionEntryRepository;
import com.ask.repository.RoleRepository;
import com.ask.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommissionServiceImplTest {

    @Mock
    private CommissionConfigRepository configRepository;
    @Mock
    private CommissionEntryRepository entryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private CommissionServiceImpl commissionService;

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
    void getConfigsThrowsAccessDeniedForNonAdmin() {
        User receptionist = user(2L, "receptionist@askhealth.in", "RECEPTIONIST");
        when(userRepository.findByEmail(receptionist.getEmail())).thenReturn(Optional.of(receptionist));

        assertThrows(AccessDeniedException.class, () ->
                commissionService.getConfigs(receptionist.getEmail())
        );
    }

    @Test
    void getConfigsReturnsListForSuperAdmin() {
        User superAdmin = user(1L, "admin@askhealth.in", "SUPER_ADMIN");
        when(userRepository.findByEmail(superAdmin.getEmail())).thenReturn(Optional.of(superAdmin));

        Role receptionistRole = Role.builder().id(2L).name("RECEPTIONIST").displayName("Receptionist").build();
        CommissionConfig config = CommissionConfig.builder()
                .id(10L)
                .role(receptionistRole)
                .percentage(BigDecimal.valueOf(5.0))
                .updatedBy(superAdmin)
                .build();
        config.setUpdatedAt(LocalDateTime.now());

        when(configRepository.findAll()).thenReturn(List.of(config));

        List<CommissionConfigResponse> response = commissionService.getConfigs(superAdmin.getEmail());

        assertEquals(1, response.size());
        assertEquals(BigDecimal.valueOf(5.0), response.get(0).getPercentage());
        assertEquals("RECEPTIONIST", response.get(0).getRoleName());
    }

    @Test
    void updateConfigUpdatesExistingConfig() {
        User superAdmin = user(1L, "admin@askhealth.in", "SUPER_ADMIN");
        when(userRepository.findByEmail(superAdmin.getEmail())).thenReturn(Optional.of(superAdmin));

        Role receptionistRole = Role.builder().id(2L).name("RECEPTIONIST").displayName("Receptionist").build();
        when(roleRepository.findById(2L)).thenReturn(Optional.of(receptionistRole));

        CommissionConfig config = CommissionConfig.builder()
                .id(10L)
                .role(receptionistRole)
                .percentage(BigDecimal.valueOf(5.0))
                .build();

        when(configRepository.findByRoleId(2L)).thenReturn(Optional.of(config));
        when(configRepository.save(any(CommissionConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommissionConfigRequest request = CommissionConfigRequest.builder()
                .roleId(2L)
                .percentage(BigDecimal.valueOf(7.5))
                .build();

        CommissionConfigResponse response = commissionService.updateConfig(request, superAdmin.getEmail());

        assertEquals(BigDecimal.valueOf(7.5), response.getPercentage());
        assertEquals("RECEPTIONIST", response.getRoleName());
        verify(configRepository).save(config);
    }

    @Test
    void getCommissionsCallsRepositoryWithSpecification() {
        User superAdmin = user(1L, "admin@askhealth.in", "SUPER_ADMIN");
        when(userRepository.findByEmail(superAdmin.getEmail())).thenReturn(Optional.of(superAdmin));

        Page<CommissionEntry> emptyPage = new PageImpl<>(List.of());
        when(entryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        var result = commissionService.getCommissions(null, null, "2026-05", "PENDING", 0, 10, superAdmin.getEmail());

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void getCommissionSummaryAggregatesPayouts() {
        User superAdmin = user(1L, "admin@askhealth.in", "SUPER_ADMIN");
        when(userRepository.findByEmail(superAdmin.getEmail())).thenReturn(Optional.of(superAdmin));

        Object[] summaryRow = new Object[]{1L, "John Doe", "RECEPTIONIST", "2026-05", BigDecimal.valueOf(1500.00)};
        List<Object[]> summaryList = new ArrayList<>();
        summaryList.add(summaryRow);
        when(entryRepository.findMonthlySummary("2026-05")).thenReturn(summaryList);

        List<CommissionSummaryResponse> summaries = commissionService.getCommissionSummary("2026-05", superAdmin.getEmail());

        assertEquals(1, summaries.size());
        assertEquals("John Doe", summaries.get(0).getFullName());
        assertEquals(BigDecimal.valueOf(1500.00), summaries.get(0).getTotalAmount());
    }
}
