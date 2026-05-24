package com.ask.service;

import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.notification.NotificationResponse;
import com.ask.entity.User;
import com.ask.enums.NotificationType;

/**
 * Service interface for managing in-app notifications.
 */
public interface NotificationService {

    PageResponse<NotificationResponse> getNotifications(String email, int page, int size);

    long getUnreadCount(String email);

    void markAsRead(Long id, String email);

    void markAllAsRead(String email);

    void sendNotification(User recipient, NotificationType type, String title, String message, String referenceType, Long referenceId);
}
