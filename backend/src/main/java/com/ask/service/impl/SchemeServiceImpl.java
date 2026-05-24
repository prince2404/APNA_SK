package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.request.billing.SchemeRequest;
import com.ask.dto.response.billing.SchemeResponse;
import com.ask.entity.*;
import com.ask.enums.EntityStatus;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.GeographicScopeException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.mapper.SchemeMapper;
import com.ask.repository.ProductCategoryRepository;
import com.ask.repository.SchemeRepository;
import com.ask.repository.StateRepository;
import com.ask.repository.UserRepository;
import com.ask.service.SchemeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchemeServiceImpl implements SchemeService {

    private final SchemeRepository schemeRepository;
    private final UserRepository userRepository;
    private final StateRepository stateRepository;
    private final ProductCategoryRepository categoryRepository;
    private final SchemeMapper schemeMapper;

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void ensureAdminPrivileges(User user) {
        String role = user.getRole().getName();
        if (!role.equals(RoleConstants.SUPER_ADMIN)
                && !role.equals(RoleConstants.SYSTEM_ADMIN)
                && !role.equals(RoleConstants.STATE_ADMIN)) {
            throw new AccessDeniedException("Access denied. Admin privileges required to manage schemes.");
        }
    }

    @Override
    @Transactional
    public SchemeResponse createScheme(SchemeRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensureAdminPrivileges(currentUser);

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessRuleException("Start date cannot be after end date");
        }

        String role = currentUser.getRole().getName();
        final Long finalStateId;
        if (RoleConstants.STATE_ADMIN.equals(role)) {
            finalStateId = currentUser.getState().getId();
        } else {
            finalStateId = request.getStateId();
        }

        State state = null;
        if (finalStateId != null) {
            state = stateRepository.findById(finalStateId)
                    .orElseThrow(() -> new ResourceNotFoundException("State", "id", finalStateId));
        }

        ProductCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", "id", request.getCategoryId()));
        }

        Scheme scheme = Scheme.builder()
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .category(category)
                .state(state)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(EntityStatus.ACTIVE)
                .createdBy(currentUser)
                .build();

        Scheme saved = schemeRepository.save(scheme);
        return schemeMapper.toSchemeResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchemeResponse> getSchemes(String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        String role = currentUser.getRole().getName();
        List<Scheme> schemes;

        if (RoleConstants.SUPER_ADMIN.equals(role) || RoleConstants.SYSTEM_ADMIN.equals(role)) {
            schemes = schemeRepository.findAll();
        } else {
            Long userStateId = currentUser.getState() != null ? currentUser.getState().getId() : null;
            schemes = schemeRepository.findAll().stream()
                    .filter(s -> s.getState() == null || s.getState().getId().equals(userStateId))
                    .toList();
        }

        return schemes.stream()
                .map(schemeMapper::toSchemeResponse)
                .toList();
    }

    @Override
    @Transactional
    public void toggleSchemeStatus(Long id, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensureAdminPrivileges(currentUser);

        Scheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheme", "id", id));

        String role = currentUser.getRole().getName();
        if (RoleConstants.STATE_ADMIN.equals(role)) {
            if (scheme.getState() == null || !scheme.getState().getId().equals(currentUser.getState().getId())) {
                throw new GeographicScopeException("Access denied. You can only manage schemes in your state.");
            }
        }

        scheme.setStatus(scheme.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        schemeRepository.save(scheme);
    }
}
