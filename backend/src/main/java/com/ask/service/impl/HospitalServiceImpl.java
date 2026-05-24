package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.request.hospital.HospitalRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.hospital.HospitalResponse;
import com.ask.entity.District;
import com.ask.entity.Hospital;
import com.ask.entity.State;
import com.ask.entity.User;
import com.ask.enums.EntityStatus;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.GeographicScopeException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.mapper.HospitalMapper;
import com.ask.repository.DistrictRepository;
import com.ask.repository.HospitalRepository;
import com.ask.repository.StateRepository;
import com.ask.repository.UserRepository;
import com.ask.service.HospitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final HospitalMapper hospitalMapper;

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void ensureAdminRole(User user) {
        String role = user.getRole().getName();
        if (!role.equals(RoleConstants.SUPER_ADMIN)
                && !role.equals(RoleConstants.SYSTEM_ADMIN)
                && !role.equals(RoleConstants.STATE_ADMIN)) {
            throw new AccessDeniedException("Access denied. Admin privileges required.");
        }
    }

    private void ensureGeographicScope(User user, State state, District district) {
        String roleName = user.getRole().getName();
        if (RoleConstants.SUPER_ADMIN.equals(roleName) || RoleConstants.SYSTEM_ADMIN.equals(roleName)) {
            return;
        }
        if (user.getState() != null && !user.getState().getId().equals(state.getId())) {
            throw new GeographicScopeException("State is outside user's assigned scope");
        }
        if (user.getDistrict() != null && !user.getDistrict().getId().equals(district.getId())) {
            throw new GeographicScopeException("District is outside user's assigned scope");
        }
    }

    @Override
    @Transactional
    public HospitalResponse createHospital(HospitalRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensureAdminRole(currentUser);

        State state = stateRepository.findById(request.getStateId())
                .orElseThrow(() -> new ResourceNotFoundException("State", "id", request.getStateId()));
        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", request.getDistrictId()));

        if (!district.getState().getId().equals(state.getId())) {
            throw new BusinessRuleException("District does not belong to the selected state");
        }

        ensureGeographicScope(currentUser, state, district);

        Hospital hospital = Hospital.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .contactPerson(request.getContactPerson())
                .state(state)
                .district(district)
                .status(EntityStatus.ACTIVE)
                .build();

        Hospital saved = hospitalRepository.save(hospital);
        return hospitalMapper.toHospitalResponse(saved);
    }

    @Override
    @Transactional
    public HospitalResponse updateHospital(Long id, HospitalRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensureAdminRole(currentUser);

        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", "id", id));

        State state = stateRepository.findById(request.getStateId())
                .orElseThrow(() -> new ResourceNotFoundException("State", "id", request.getStateId()));
        District district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", request.getDistrictId()));

        if (!district.getState().getId().equals(state.getId())) {
            throw new BusinessRuleException("District does not belong to the selected state");
        }

        ensureGeographicScope(currentUser, hospital.getState(), hospital.getDistrict());
        ensureGeographicScope(currentUser, state, district);

        hospital.setName(request.getName());
        hospital.setAddress(request.getAddress());
        hospital.setPhone(request.getPhone());
        hospital.setContactPerson(request.getContactPerson());
        hospital.setState(state);
        hospital.setDistrict(district);

        Hospital saved = hospitalRepository.save(hospital);
        return hospitalMapper.toHospitalResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HospitalResponse> getHospitals(Long stateId, Long districtId, int page, int size, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Hospital> hospitalPage;

        // Resolve geographic restrictions
        String roleName = currentUser.getRole().getName();
        boolean isPlatformAdmin = RoleConstants.SUPER_ADMIN.equals(roleName) || RoleConstants.SYSTEM_ADMIN.equals(roleName);

        if (!isPlatformAdmin) {
            if (currentUser.getState() != null) {
                stateId = currentUser.getState().getId();
            }
            if (currentUser.getDistrict() != null) {
                districtId = currentUser.getDistrict().getId();
            }
        }

        if (districtId != null) {
            hospitalPage = hospitalRepository.findByDistrictId(districtId, pageable);
        } else if (stateId != null) {
            hospitalPage = hospitalRepository.findByStateId(stateId, pageable);
        } else {
            hospitalPage = hospitalRepository.findAll(pageable);
        }

        List<HospitalResponse> content = hospitalPage.getContent().stream()
                .map(hospitalMapper::toHospitalResponse)
                .collect(Collectors.toList());

        return PageResponse.of(hospitalPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public HospitalResponse getHospitalById(Long id, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", "id", id));

        ensureGeographicScope(currentUser, hospital.getState(), hospital.getDistrict());

        return hospitalMapper.toHospitalResponse(hospital);
    }

    @Override
    @Transactional
    public void toggleHospitalStatus(Long id, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensureAdminRole(currentUser);

        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", "id", id));

        ensureGeographicScope(currentUser, hospital.getState(), hospital.getDistrict());

        hospital.setStatus(hospital.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        hospitalRepository.save(hospital);
    }
}
