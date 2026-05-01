package com.ask.service.impl;

import com.ask.dto.request.geography.DistrictRequest;
import com.ask.dto.response.geography.DistrictResponse;
import com.ask.dto.response.geography.StateResponse;
import com.ask.entity.Block;
import com.ask.entity.District;
import com.ask.entity.Role;
import com.ask.entity.State;
import com.ask.entity.Store;
import com.ask.entity.User;
import com.ask.exception.GeographicScopeException;
import com.ask.mapper.GeographyMapper;
import com.ask.repository.BlockRepository;
import com.ask.repository.DistrictRepository;
import com.ask.repository.StateRepository;
import com.ask.repository.StoreRepository;
import com.ask.repository.UserRepository;
import com.ask.service.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeographyServiceImplTest {

    @Mock
    private StateRepository stateRepository;
    @Mock
    private DistrictRepository districtRepository;
    @Mock
    private BlockRepository blockRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GeographyMapper geographyMapper;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private GeographyServiceImpl geographyService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createDistrictRejectsParentStateOutsideCurrentUserScope() {
        State bihar = State.builder().id(1L).name("Bihar").code("BR").build();
        User stateAdmin = User.builder()
                .id(10L)
                .email("state-admin@askhealth.in")
                .role(Role.builder().name("STATE_ADMIN").displayName("State Admin").build())
                .state(bihar)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(stateAdmin.getEmail(), null));

        when(userRepository.findByEmail(stateAdmin.getEmail())).thenReturn(Optional.of(stateAdmin));

        DistrictRequest request = DistrictRequest.builder()
                .name("Lucknow")
                .stateId(2L)
                .build();

        assertThrows(GeographicScopeException.class, () -> geographyService.createDistrict(request));
        verify(districtRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void createDistrictWritesAuditLogAfterSaving() {
        State bihar = State.builder().id(1L).name("Bihar").code("BR").build();
        User superAdmin = User.builder()
                .id(1L)
                .email("admin@askhealth.in")
                .role(Role.builder().name("SUPER_ADMIN").displayName("Super Admin").build())
                .build();
        District savedDistrict = District.builder().id(20L).name("Patna").state(bihar).build();
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(superAdmin.getEmail(), null));

        when(userRepository.findByEmail(superAdmin.getEmail())).thenReturn(Optional.of(superAdmin));
        when(stateRepository.findById(bihar.getId())).thenReturn(Optional.of(bihar));
        when(districtRepository.existsByNameIgnoreCaseAndStateId("Patna", bihar.getId())).thenReturn(false);
        when(districtRepository.save(any(District.class))).thenReturn(savedDistrict);
        when(geographyMapper.toDistrictResponse(savedDistrict)).thenReturn(
                DistrictResponse.builder().id(savedDistrict.getId()).name(savedDistrict.getName()).build());

        geographyService.createDistrict(DistrictRequest.builder()
                .name("Patna")
                .stateId(bihar.getId())
                .build());

        verify(auditService).log(eq(superAdmin), eq("CREATE_DISTRICT"), eq("DISTRICT"),
                eq(savedDistrict.getId()), eq(null), eq(null), eq(null), any());
    }

    @Test
    void storeScopedUserCanReadOwnParentStateWithoutDirectStateAssignment() {
        State bihar = State.builder().id(1L).name("Bihar").code("BR").build();
        District patna = District.builder().id(2L).name("Patna").state(bihar).build();
        Block block = Block.builder().id(3L).name("Phulwari").district(patna).build();
        Store store = Store.builder().id(4L).name("ASK Patna").code("PAT01").block(block).build();
        User receptionist = User.builder()
                .id(11L)
                .email("receptionist@askhealth.in")
                .role(Role.builder().name("RECEPTIONIST").displayName("Receptionist").build())
                .store(store)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(receptionist.getEmail(), null));

        when(userRepository.findByEmail(receptionist.getEmail())).thenReturn(Optional.of(receptionist));
        when(stateRepository.findById(bihar.getId())).thenReturn(Optional.of(bihar));
        when(geographyMapper.toStateResponse(bihar)).thenReturn(
                StateResponse.builder().id(bihar.getId()).name(bihar.getName()).code(bihar.getCode()).build());

        geographyService.getStateById(bihar.getId());

        verify(stateRepository).findById(bihar.getId());
    }
}
