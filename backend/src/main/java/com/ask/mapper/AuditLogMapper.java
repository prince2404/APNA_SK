package com.ask.mapper;

import com.ask.dto.response.audit.AuditLogResponse;
import com.ask.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for AuditLog entity to response DTO.
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userFullName")
    AuditLogResponse toResponse(AuditLog auditLog);
}
