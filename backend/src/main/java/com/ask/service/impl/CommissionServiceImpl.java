package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.request.commission.CommissionConfigRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.commission.CommissionConfigResponse;
import com.ask.dto.response.commission.CommissionSummaryResponse;
import com.ask.entity.*;
import com.ask.enums.CommissionStatus;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.repository.CommissionConfigRepository;
import com.ask.repository.CommissionEntryRepository;
import com.ask.repository.RoleRepository;
import com.ask.repository.UserRepository;
import com.ask.service.CommissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private final CommissionConfigRepository configRepository;
    private final CommissionEntryRepository entryRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void ensureSuperOrSystemAdmin(User user) {
        String role = user.getRole().getName();
        if (!role.equals(RoleConstants.SUPER_ADMIN) && !role.equals(RoleConstants.SYSTEM_ADMIN)) {
            throw new AccessDeniedException("Access denied. Super Admin or System Admin role required.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionConfigResponse> getConfigs(String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensureSuperOrSystemAdmin(currentUser);

        List<CommissionConfig> configs = configRepository.findAll();
        return configs.stream()
                .map(c -> CommissionConfigResponse.builder()
                        .id(c.getId())
                        .roleId(c.getRole().getId())
                        .roleName(c.getRole().getName())
                        .percentage(c.getPercentage())
                        .updatedByName(c.getUpdatedBy() != null ? c.getUpdatedBy().getFullName() : "System Seed")
                        .updatedAt(c.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public CommissionConfigResponse updateConfig(CommissionConfigRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensureSuperOrSystemAdmin(currentUser);

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        CommissionConfig config = configRepository.findByRoleId(request.getRoleId())
                .orElseGet(() -> CommissionConfig.builder()
                        .role(role)
                        .percentage(BigDecimal.ZERO)
                        .build());

        config.setPercentage(request.getPercentage());
        config.setUpdatedBy(currentUser);

        CommissionConfig saved = configRepository.save(config);

        return CommissionConfigResponse.builder()
                .id(saved.getId())
                .roleId(saved.getRole().getId())
                .roleName(saved.getRole().getName())
                .percentage(saved.getPercentage())
                .updatedByName(currentUser.getFullName())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommissionEntry> getCommissions(Long userId, Long roleId, String month, String status, int page, int size, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        String currentRole = currentUser.getRole().getName();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<CommissionEntry> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Scoping based on logged-in user role
            if (RoleConstants.STATE_ADMIN.equals(currentRole)) {
                predicates.add(cb.equal(root.get("user").get("state").get("id"), currentUser.getState().getId()));
            } else if (RoleConstants.DISTRICT_ADMIN.equals(currentRole)) {
                predicates.add(cb.equal(root.get("user").get("district").get("id"), currentUser.getDistrict().getId()));
            } else if (RoleConstants.BLOCK_ADMIN.equals(currentRole)) {
                predicates.add(cb.equal(root.get("user").get("block").get("id"), currentUser.getBlock().getId()));
            } else if (!RoleConstants.SUPER_ADMIN.equals(currentRole) && !RoleConstants.SYSTEM_ADMIN.equals(currentRole)) {
                // Receptionist, Volunteer, Pharmacist can only see their own commissions
                predicates.add(cb.equal(root.get("user").get("id"), currentUser.getId()));
            }

            // Request parameter filters
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (roleId != null) {
                predicates.add(cb.equal(root.get("role").get("id"), roleId));
            }
            if (month != null && !month.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("month"), month.trim()));
            }
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), CommissionStatus.valueOf(status.toUpperCase().trim())));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CommissionEntry> entryPage = entryRepository.findAll(spec, pageable);
        return PageResponse.of(entryPage, entryPage.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionSummaryResponse> getCommissionSummary(String month, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensureSuperOrSystemAdmin(currentUser);

        List<Object[]> rows = entryRepository.findMonthlySummary(month.trim());
        return rows.stream().map(row -> CommissionSummaryResponse.builder()
                .userId((Long) row[0])
                .fullName((String) row[1])
                .roleName((String) row[2])
                .month((String) row[3])
                .totalAmount((BigDecimal) row[4])
                .build())
                .toList();
    }
}
