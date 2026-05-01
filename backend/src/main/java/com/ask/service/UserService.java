package com.ask.service;

import com.ask.dto.request.user.PermissionAssignmentRequest;
import com.ask.dto.request.user.UserCreateRequest;
import com.ask.dto.request.user.UserUpdateRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.user.PermissionResponse;
import com.ask.dto.response.user.UserResponse;
import com.ask.enums.UserStatus;

import java.util.List;

/**
 * User management service for admin-controlled user CRUD and permission assignment.
 */
public interface UserService {

    UserResponse createUser(UserCreateRequest request, String currentUserEmail);

    UserResponse getUserById(Long id, String currentUserEmail);

    PageResponse<UserResponse> getUsers(String currentUserEmail, String search, Long roleId,
                                        UserStatus status, int page, int size);

    UserResponse updateUser(Long id, UserUpdateRequest request, String currentUserEmail);

    void deactivateUser(Long id, String currentUserEmail);

    void reactivateUser(Long id, String currentUserEmail);

    UserResponse assignPermissions(Long id, PermissionAssignmentRequest request, String currentUserEmail);

    List<PermissionResponse> getAllPermissions();
}
