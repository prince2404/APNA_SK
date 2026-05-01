package com.ask.mapper;

import com.ask.dto.response.permission.PermissionRequestResponse;
import com.ask.entity.Permission;
import com.ask.entity.PermissionRequest;
import com.ask.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-01T13:43:14+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PermissionRequestMapperImpl implements PermissionRequestMapper {

    @Override
    public PermissionRequestResponse toResponse(PermissionRequest request) {
        if ( request == null ) {
            return null;
        }

        PermissionRequestResponse.PermissionRequestResponseBuilder permissionRequestResponse = PermissionRequestResponse.builder();

        permissionRequestResponse.userId( requestUserId( request ) );
        permissionRequestResponse.userFullName( requestUserFullName( request ) );
        permissionRequestResponse.userEmail( requestUserEmail( request ) );
        permissionRequestResponse.permissionId( requestPermissionId( request ) );
        permissionRequestResponse.permissionDescription( requestPermissionDescription( request ) );
        permissionRequestResponse.reviewedById( requestReviewedById( request ) );
        permissionRequestResponse.reviewedByFullName( requestReviewedByFullName( request ) );
        permissionRequestResponse.id( request.getId() );
        permissionRequestResponse.reason( request.getReason() );
        permissionRequestResponse.status( request.getStatus() );
        permissionRequestResponse.reviewedAt( request.getReviewedAt() );
        permissionRequestResponse.createdAt( request.getCreatedAt() );

        permissionRequestResponse.permissionCode( request.getPermission().getModule() + ":" + request.getPermission().getAction() );

        return permissionRequestResponse.build();
    }

    private Long requestUserId(PermissionRequest permissionRequest) {
        User user = permissionRequest.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }

    private String requestUserFullName(PermissionRequest permissionRequest) {
        User user = permissionRequest.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getFullName();
    }

    private String requestUserEmail(PermissionRequest permissionRequest) {
        User user = permissionRequest.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getEmail();
    }

    private Long requestPermissionId(PermissionRequest permissionRequest) {
        Permission permission = permissionRequest.getPermission();
        if ( permission == null ) {
            return null;
        }
        return permission.getId();
    }

    private String requestPermissionDescription(PermissionRequest permissionRequest) {
        Permission permission = permissionRequest.getPermission();
        if ( permission == null ) {
            return null;
        }
        return permission.getDescription();
    }

    private Long requestReviewedById(PermissionRequest permissionRequest) {
        User reviewedBy = permissionRequest.getReviewedBy();
        if ( reviewedBy == null ) {
            return null;
        }
        return reviewedBy.getId();
    }

    private String requestReviewedByFullName(PermissionRequest permissionRequest) {
        User reviewedBy = permissionRequest.getReviewedBy();
        if ( reviewedBy == null ) {
            return null;
        }
        return reviewedBy.getFullName();
    }
}
