package com.ask.service.impl;

import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.notification.NotificationResponse;
import com.ask.entity.Notification;
import com.ask.entity.User;
import com.ask.enums.NotificationType;
import com.ask.exception.ResourceNotFoundException;
import com.ask.repository.NotificationRepository;
import com.ask.repository.UserRepository;
import com.ask.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(String email, int page, int size) {
        User user = getUserByEmail(email);
        Page<Notification> notificationPage = notificationRepository.findByUserId(
                user.getId(), PageRequest.of(page, size, Sort.by("createdAt").descending()));
        
        List<NotificationResponse> content = notificationPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.of(notificationPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        User user = getUserByEmail(email);
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Override
    @Transactional
    public void markAsRead(Long id, String email) {
        User user = getUserByEmail(email);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not own this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String email) {
        User user = getUserByEmail(email);
        notificationRepository.markAllAsReadForUser(user.getId());
    }

    @Override
    @Transactional
    public void sendNotification(User recipient, NotificationType type, String title, String message, String referenceType, Long referenceId) {
        Notification notification = Notification.builder()
                .user(recipient)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
        log.info("In-app notification sent to user: {}, title: {}", recipient.getEmail(), title);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
