package com.ask.mapper;

import com.ask.dto.response.audit.AuditLogResponse;
import com.ask.entity.AuditLog;
import com.ask.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-01T13:43:14+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AuditLogMapperImpl implements AuditLogMapper {

    @Override
    public AuditLogResponse toResponse(AuditLog auditLog) {
        if ( auditLog == null ) {
            return null;
        }

        AuditLogResponse.AuditLogResponseBuilder auditLogResponse = AuditLogResponse.builder();

        auditLogResponse.userId( auditLogUserId( auditLog ) );
        auditLogResponse.userFullName( auditLogUserFullName( auditLog ) );
        auditLogResponse.id( auditLog.getId() );
        auditLogResponse.action( auditLog.getAction() );
        auditLogResponse.entityType( auditLog.getEntityType() );
        auditLogResponse.entityId( auditLog.getEntityId() );
        auditLogResponse.description( auditLog.getDescription() );
        auditLogResponse.ipAddress( auditLog.getIpAddress() );
        auditLogResponse.createdAt( auditLog.getCreatedAt() );

        return auditLogResponse.build();
    }

    private Long auditLogUserId(AuditLog auditLog) {
        User user = auditLog.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }

    private String auditLogUserFullName(AuditLog auditLog) {
        User user = auditLog.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getFullName();
    }
}
