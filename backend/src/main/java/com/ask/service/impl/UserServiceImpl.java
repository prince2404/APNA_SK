package com.ask.service.impl;

import com.ask.constants.AppConstants;
import com.ask.constants.ErrorMessages;
import com.ask.constants.RoleConstants;
import com.ask.dto.request.user.PermissionAssignmentRequest;
import com.ask.dto.request.user.UserCreateRequest;
import com.ask.dto.request.user.UserUpdateRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.user.PermissionResponse;
import com.ask.dto.response.user.UserResponse;
import com.ask.entity.*;
import com.ask.enums.UserStatus;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.DuplicateResourceException;
import com.ask.exception.GeographicScopeException;
import com.ask.exception.InvalidRequestException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.mapper.UserMapper;
import com.ask.repository.*;
import com.ask.service.AuditService;
import com.ask.service.UserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * User management implementation with role hierarchy and geographic scoping.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final BlockRepository blockRepository;
    private final StoreRepository storeRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final TwoFactorConfigRepository twoFactorConfigRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        Role targetRole = getRole(request.getRoleId());
        ensureCanManageRole(currentUser, targetRole);
        validateAadhaarLastFour(request.getAadhaarLastFour());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(ErrorMessages.EMAIL_ALREADY_EXISTS);
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException(ErrorMessages.PHONE_ALREADY_EXISTS);
        }

        GeographyAssignment geography = resolveGeography(
                targetRole, request.getStateId(), request.getDistrictId(), request.getBlockId(), request.getStoreId());
        ensureGeographyInScope(currentUser, geography);

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase(Locale.ROOT))
                .phone(blankToNull(request.getPhone()))
                .passwordHash(passwordEncoder.encode(request.getTemporaryPassword()))
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .role(targetRole)
                .state(geography.state())
                .district(geography.district())
                .block(geography.block())
                .store(geography.store())
                .aadhaarLastFour(blankToNull(request.getAadhaarLastFour()))
                .panNumber(blankToNull(request.getPanNumber()))
                .status(UserStatus.ACTIVE)
                .forcePasswordChange(true)
                .createdBy(currentUser)
                .build();

        User saved = userRepository.save(user);
        createTwoFactorConfigIfNeeded(saved);
        replacePermissions(saved, currentUser, request.getPermissionIds());
        auditService.log(currentUser, "CREATE_USER", "USER", saved.getId(), null, null, null,
                "Created user: " + saved.getEmail());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        User user = getUser(id);
        ensureUserVisible(currentUser, user);
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(String currentUserEmail, String search, Long roleId,
                                               UserStatus status, int page, int size) {
        User currentUser = getCurrentUser(currentUserEmail);
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Page<User> userPage = userRepository.findAll(userSpecification(currentUser, search, roleId, status),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<UserResponse> content = userPage.getContent().stream().map(this::toResponse).toList();
        return PageResponse.of(userPage, content);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        User user = getUser(id);
        ensureUserVisible(currentUser, user);
        Role targetRole = request.getRoleId() != null ? getRole(request.getRoleId()) : user.getRole();
        ensureCanManageRole(currentUser, targetRole);
        validateAadhaarLastFour(request.getAadhaarLastFour());

        String email = request.getEmail().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateResourceException(ErrorMessages.EMAIL_ALREADY_EXISTS);
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhoneAndIdNot(request.getPhone(), id)) {
            throw new DuplicateResourceException(ErrorMessages.PHONE_ALREADY_EXISTS);
        }

        GeographyAssignment geography = resolveGeography(
                targetRole, request.getStateId(), request.getDistrictId(), request.getBlockId(), request.getStoreId());
        ensureGeographyInScope(currentUser, geography);

        user.setFullName(request.getFullName());
        user.setEmail(email);
        user.setPhone(blankToNull(request.getPhone()));
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());
        user.setRole(targetRole);
        user.setState(geography.state());
        user.setDistrict(geography.district());
        user.setBlock(geography.block());
        user.setStore(geography.store());
        user.setAadhaarLastFour(blankToNull(request.getAadhaarLastFour()));
        user.setPanNumber(blankToNull(request.getPanNumber()));

        User saved = userRepository.save(user);
        createTwoFactorConfigIfNeeded(saved);
        auditService.log(currentUser, "UPDATE_USER", "USER", saved.getId(), null, null, null,
                "Updated user: " + saved.getEmail());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensureSuperAdmin(currentUser);
        User user = getUser(id);
        if (currentUser.getId().equals(user.getId())) {
            throw new BusinessRuleException(ErrorMessages.CANNOT_DEACTIVATE_SELF);
        }
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        auditService.log(currentUser, "DEACTIVATE_USER", "USER", user.getId(), null, null, null,
                "Deactivated user: " + user.getEmail());
    }

    @Override
    @Transactional
    public void reactivateUser(Long id, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensureSuperAdmin(currentUser);
        User user = getUser(id);
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        auditService.log(currentUser, "REACTIVATE_USER", "USER", user.getId(), null, null, null,
                "Reactivated user: " + user.getEmail());
    }

    @Override
    @Transactional
    public UserResponse assignPermissions(Long id, PermissionAssignmentRequest request, String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        User user = getUser(id);
        ensureUserVisible(currentUser, user);
        ensureCanManageRole(currentUser, user.getRole());
        replacePermissions(user, currentUser, request.getPermissionIds());
        auditService.log(currentUser, "ASSIGN_USER_PERMISSIONS", "USER", user.getId(), null, null, null,
                "Updated permissions for user: " + user.getEmail());
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll(Sort.by("module").ascending().and(Sort.by("action").ascending()))
                .stream()
                .map(userMapper::toPermissionResponse)
                .toList();
    }

    private void replacePermissions(User user, User grantedBy, List<Long> permissionIds) {
        userPermissionRepository.deleteByUserId(user.getId());
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.stream().distinct().count()) {
            throw new InvalidRequestException("One or more permission IDs are invalid");
        }
        List<UserPermission> grants = permissions.stream()
                .map(permission -> UserPermission.builder()
                        .user(user)
                        .permission(permission)
                        .grantedBy(grantedBy)
                        .grantedAt(LocalDateTime.now())
                        .build())
                .toList();
        userPermissionRepository.saveAll(grants);
    }

    private void createTwoFactorConfigIfNeeded(User user) {
        if (twoFactorConfigRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }
        String roleName = user.getRole().getName();
        boolean mandatory = RoleConstants.SUPER_ADMIN.equals(roleName) || RoleConstants.SYSTEM_ADMIN.equals(roleName);
        boolean optional = RoleConstants.STATE_ADMIN.equals(roleName) || RoleConstants.DISTRICT_ADMIN.equals(roleName);
        if (mandatory || optional) {
            twoFactorConfigRepository.save(TwoFactorConfig.builder()
                    .user(user)
                    .isEnabled(mandatory)
                    .isMandatory(mandatory)
                    .build());
        }
    }

    private Specification<User> userSpecification(User currentUser, String search, Long roleId, UserStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
                        criteriaBuilder.like(root.get("phone"), "%" + search + "%")
                ));
            }
            if (roleId != null) {
                predicates.add(criteriaBuilder.equal(root.get("role").get("id"), roleId));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (!hasPlatformScope(currentUser)) {
                addGeographyPredicate(predicates, root, criteriaBuilder, currentUser);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addGeographyPredicate(List<Predicate> predicates, jakarta.persistence.criteria.Root<User> root,
                                       jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder, User currentUser) {
        if (currentUser.getStore() != null) {
            predicates.add(criteriaBuilder.equal(root.get("store").get("id"), currentUser.getStore().getId()));
        } else if (currentUser.getBlock() != null) {
            predicates.add(criteriaBuilder.equal(root.get("block").get("id"), currentUser.getBlock().getId()));
        } else if (currentUser.getDistrict() != null) {
            predicates.add(criteriaBuilder.equal(root.get("district").get("id"), currentUser.getDistrict().getId()));
        } else {
            predicates.add(criteriaBuilder.equal(root.get("state").get("id"), requireStateId(currentUser)));
        }
    }

    private GeographyAssignment resolveGeography(Role role, Long stateId, Long districtId, Long blockId, Long storeId) {
        State state = stateId != null ? getState(stateId) : null;
        District district = districtId != null ? getDistrict(districtId) : null;
        Block block = blockId != null ? getBlock(blockId) : null;
        Store store = storeId != null ? getStore(storeId) : null;

        if (store != null) {
            block = store.getBlock();
            district = block.getDistrict();
            state = district.getState();
        } else if (block != null) {
            district = block.getDistrict();
            state = district.getState();
        } else if (district != null) {
            state = district.getState();
        }

        validateRequiredGeography(role, state, district, block, store);
        return new GeographyAssignment(state, district, block, store);
    }

    private void validateRequiredGeography(Role role, State state, District district, Block block, Store store) {
        switch (role.getName()) {
            case RoleConstants.STATE_ADMIN -> require(state != null, "State is required for State Admin");
            case RoleConstants.DISTRICT_ADMIN -> require(district != null, "District is required for District Admin");
            case RoleConstants.BLOCK_ADMIN -> require(block != null, "Block is required for Block Admin");
            case RoleConstants.RECEPTIONIST, RoleConstants.VOLUNTEER -> require(store != null,
                    "Store is required for Receptionist and Volunteer");
            default -> {
                // Platform and pharmacist users may be global or assigned later.
            }
        }
    }

    private void ensureUserVisible(User currentUser, User targetUser) {
        if (!hasPlatformScope(currentUser) && !isGeographyWithinScope(currentUser, targetUser.getState(),
                targetUser.getDistrict(), targetUser.getBlock(), targetUser.getStore())) {
            throw new GeographicScopeException();
        }
    }

    private void ensureGeographyInScope(User currentUser, GeographyAssignment geography) {
        if (!hasPlatformScope(currentUser) && !isGeographyWithinScope(currentUser, geography.state(),
                geography.district(), geography.block(), geography.store())) {
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
        Long currentStateId = requireStateId(currentUser);
        return state != null && currentStateId.equals(state.getId());
    }

    private void ensureCanManageRole(User currentUser, Role targetRole) {
        if (RoleConstants.SUPER_ADMIN.equals(currentUser.getRole().getName())) {
            return;
        }
        if (targetRole.getHierarchyLevel() <= currentUser.getRole().getHierarchyLevel()) {
            throw new BusinessRuleException(ErrorMessages.ROLE_HIERARCHY_VIOLATION);
        }
    }

    private void ensureSuperAdmin(User user) {
        if (!RoleConstants.SUPER_ADMIN.equals(user.getRole().getName())) {
            throw new BusinessRuleException(ErrorMessages.ONLY_SUPER_ADMIN_CAN_DEACTIVATE);
        }
    }

    private boolean hasPlatformScope(User user) {
        String roleName = user.getRole().getName();
        return RoleConstants.SUPER_ADMIN.equals(roleName) || RoleConstants.SYSTEM_ADMIN.equals(roleName);
    }

    private Long requireStateId(User user) {
        if (user.getState() != null) {
            return user.getState().getId();
        }
        if (user.getDistrict() != null) {
            return user.getDistrict().getState().getId();
        }
        if (user.getBlock() != null) {
            return user.getBlock().getDistrict().getState().getId();
        }
        if (user.getStore() != null) {
            return user.getStore().getBlock().getDistrict().getState().getId();
        }
        throw new GeographicScopeException();
    }

    private void validateAadhaarLastFour(String aadhaarLastFour) {
        if (aadhaarLastFour == null || aadhaarLastFour.isBlank()) {
            return;
        }
        if (aadhaarLastFour.matches("\\d{12}")) {
            throw new InvalidRequestException(ErrorMessages.AADHAAR_FULL_NUMBER_REJECTED);
        }
        if (!aadhaarLastFour.matches("\\d{4}")) {
            throw new InvalidRequestException("Aadhaar last four must contain exactly 4 digits");
        }
    }

    private UserResponse toResponse(User user) {
        UserResponse response = userMapper.toUserResponse(user);
        response.setPermissions(userPermissionRepository.findPermissionStringsByUserId(user.getId()));
        return response;
    }

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private Role getRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    private State getState(Long id) {
        return stateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("State", "id", id));
    }

    private District getDistrict(Long id) {
        return districtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("District", "id", id));
    }

    private Block getBlock(Long id) {
        return blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Block", "id", id));
    }

    private Store getStore(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new InvalidRequestException(message);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private record GeographyAssignment(State state, District district, Block block, Store store) {
    }
}
