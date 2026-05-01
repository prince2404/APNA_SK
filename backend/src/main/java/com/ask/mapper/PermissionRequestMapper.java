package com.ask.mapper;

import com.ask.dto.response.permission.PermissionRequestResponse;
import com.ask.entity.PermissionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for PermissionRequest entity to response DTO.
 */
@Mapper(componentModel = "spring")
public interface PermissionRequestMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userFullName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "permission.id", target = "permissionId")
    @Mapping(target = "permissionCode",
            expression = "java(request.getPermission().getModule() + \":\" + request.getPermission().getAction())")
    @Mapping(source = "permission.description", target = "permissionDescription")
    @Mapping(source = "reviewedBy.id", target = "reviewedById")
    @Mapping(source = "reviewedBy.fullName", target = "reviewedByFullName")
    PermissionRequestResponse toResponse(PermissionRequest request);
}
