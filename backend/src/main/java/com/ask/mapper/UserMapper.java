package com.ask.mapper;

import com.ask.dto.response.user.PermissionResponse;
import com.ask.dto.response.user.UserResponse;
import com.ask.entity.Permission;
import com.ask.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for user management DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role.id", target = "roleId")
    @Mapping(source = "role.name", target = "roleName")
    @Mapping(source = "role.displayName", target = "roleDisplayName")
    @Mapping(source = "role.hierarchyLevel", target = "hierarchyLevel")
    @Mapping(source = "state.id", target = "stateId")
    @Mapping(source = "state.name", target = "stateName")
    @Mapping(source = "district.id", target = "districtId")
    @Mapping(source = "district.name", target = "districtName")
    @Mapping(source = "block.id", target = "blockId")
    @Mapping(source = "block.name", target = "blockName")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(target = "permissions", ignore = true)
    UserResponse toUserResponse(User user);

    @Mapping(target = "code", expression = "java(permission.getModule() + \":\" + permission.getAction())")
    PermissionResponse toPermissionResponse(Permission permission);
}
