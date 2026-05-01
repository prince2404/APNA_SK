package com.ask.mapper;

import com.ask.dto.response.notification.NotificationResponse;
import com.ask.entity.Notification;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for Notification entity to response DTO.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
