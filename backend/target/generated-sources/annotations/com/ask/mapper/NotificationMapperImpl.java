package com.ask.mapper;

import com.ask.dto.response.notification.NotificationResponse;
import com.ask.entity.Notification;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-01T13:43:14+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public NotificationResponse toResponse(Notification notification) {
        if ( notification == null ) {
            return null;
        }

        NotificationResponse.NotificationResponseBuilder notificationResponse = NotificationResponse.builder();

        notificationResponse.id( notification.getId() );
        notificationResponse.type( notification.getType() );
        notificationResponse.title( notification.getTitle() );
        notificationResponse.message( notification.getMessage() );
        notificationResponse.isRead( notification.getIsRead() );
        notificationResponse.refEntityType( notification.getRefEntityType() );
        notificationResponse.refEntityId( notification.getRefEntityId() );
        notificationResponse.createdAt( notification.getCreatedAt() );

        return notificationResponse.build();
    }
}
