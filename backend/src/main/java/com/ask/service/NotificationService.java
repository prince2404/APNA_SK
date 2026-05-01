package com.ask.service;

import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.notification.NotificationResponse;
import com.ask.entity.Notification;
import com.ask.entity.User;
import com.ask.enums.NotificationType;

/**
 * Service for in-app notification management.
 */
public interface NotificationService {

    /** Returns paginated notifications for the currently authenticated user. */
    PageResponse<NotificationResponse> getMyNotifications(String currentUserEmail, Boolean unreadOnly,
                                                           int page, int size);

    /** Returns the count of unread notifications for the currently authenticated user. */
    long getUnreadCount(String currentUserEmail);

    /** Marks a single notification as read. */
    void markRead(Long notificationId, String currentUserEmail);

    /** Marks all notifications for the currently authenticated user as read. */
    void markAllRead(String currentUserEmail);

    /**
     * Creates a notification for a given user.
     * Called internally by other services (async-safe).
     */
    Notification createNotification(User user, NotificationType type, String title, String message,
                                     String refEntityType, Long refEntityId);
}
