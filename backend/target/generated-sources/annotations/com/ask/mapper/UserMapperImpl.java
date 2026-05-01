package com.ask.mapper;

import com.ask.dto.response.user.PermissionResponse;
import com.ask.dto.response.user.UserResponse;
import com.ask.entity.Block;
import com.ask.entity.District;
import com.ask.entity.Permission;
import com.ask.entity.Role;
import com.ask.entity.State;
import com.ask.entity.Store;
import com.ask.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-01T18:11:50+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.roleId( userRoleId( user ) );
        userResponse.roleName( userRoleName( user ) );
        userResponse.roleDisplayName( userRoleDisplayName( user ) );
        userResponse.hierarchyLevel( userRoleHierarchyLevel( user ) );
        userResponse.stateId( userStateId( user ) );
        userResponse.stateName( userStateName( user ) );
        userResponse.districtId( userDistrictId( user ) );
        userResponse.districtName( userDistrictName( user ) );
        userResponse.blockId( userBlockId( user ) );
        userResponse.blockName( userBlockName( user ) );
        userResponse.storeId( userStoreId( user ) );
        userResponse.storeName( userStoreName( user ) );
        userResponse.aadhaarLastFour( user.getAadhaarLastFour() );
        userResponse.address( user.getAddress() );
        userResponse.createdAt( user.getCreatedAt() );
        userResponse.dateOfBirth( user.getDateOfBirth() );
        userResponse.email( user.getEmail() );
        userResponse.forcePasswordChange( user.getForcePasswordChange() );
        userResponse.fullName( user.getFullName() );
        userResponse.gender( user.getGender() );
        userResponse.id( user.getId() );
        userResponse.lastLoginAt( user.getLastLoginAt() );
        userResponse.panNumber( user.getPanNumber() );
        userResponse.phone( user.getPhone() );
        userResponse.profilePhotoUrl( user.getProfilePhotoUrl() );
        userResponse.status( user.getStatus() );
        userResponse.updatedAt( user.getUpdatedAt() );
        userResponse.verificationStatus( user.getVerificationStatus() );

        return userResponse.build();
    }

    @Override
    public PermissionResponse toPermissionResponse(Permission permission) {
        if ( permission == null ) {
            return null;
        }

        PermissionResponse.PermissionResponseBuilder permissionResponse = PermissionResponse.builder();

        permissionResponse.action( permission.getAction() );
        permissionResponse.description( permission.getDescription() );
        permissionResponse.id( permission.getId() );
        permissionResponse.module( permission.getModule() );

        permissionResponse.code( permission.getModule() + ":" + permission.getAction() );

        return permissionResponse.build();
    }

    private Long userRoleId(User user) {
        Role role = user.getRole();
        if ( role == null ) {
            return null;
        }
        return role.getId();
    }

    private String userRoleName(User user) {
        Role role = user.getRole();
        if ( role == null ) {
            return null;
        }
        return role.getName();
    }

    private String userRoleDisplayName(User user) {
        Role role = user.getRole();
        if ( role == null ) {
            return null;
        }
        return role.getDisplayName();
    }

    private Integer userRoleHierarchyLevel(User user) {
        Role role = user.getRole();
        if ( role == null ) {
            return null;
        }
        return role.getHierarchyLevel();
    }

    private Long userStateId(User user) {
        State state = user.getState();
        if ( state == null ) {
            return null;
        }
        return state.getId();
    }

    private String userStateName(User user) {
        State state = user.getState();
        if ( state == null ) {
            return null;
        }
        return state.getName();
    }

    private Long userDistrictId(User user) {
        District district = user.getDistrict();
        if ( district == null ) {
            return null;
        }
        return district.getId();
    }

    private String userDistrictName(User user) {
        District district = user.getDistrict();
        if ( district == null ) {
            return null;
        }
        return district.getName();
    }

    private Long userBlockId(User user) {
        Block block = user.getBlock();
        if ( block == null ) {
            return null;
        }
        return block.getId();
    }

    private String userBlockName(User user) {
        Block block = user.getBlock();
        if ( block == null ) {
            return null;
        }
        return block.getName();
    }

    private Long userStoreId(User user) {
        Store store = user.getStore();
        if ( store == null ) {
            return null;
        }
        return store.getId();
    }

    private String userStoreName(User user) {
        Store store = user.getStore();
        if ( store == null ) {
            return null;
        }
        return store.getName();
    }
}
