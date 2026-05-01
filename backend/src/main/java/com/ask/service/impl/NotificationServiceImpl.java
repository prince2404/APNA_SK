package com.ask.service.impl;

import com.ask.constants.AppConstants;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.notification.NotificationResponse;
import com.ask.entity.Notification;
import com.ask.entity.User;
import com.ask.enums.NotificationType;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.mapper.NotificationMapper;
import com.ask.repository.NotificationRepository;
import com.ask.repository.UserRepository;
import com.ask.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Notification management implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(String currentUserEmail,
                                                                  Boolean unreadOnly,
                                                                  int page, int size) {
        User user = getUser(currentUserEmail);
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Notification> pageResult;
        if (Boolean.TRUE.equals(unreadOnly)) {
            pageResult = notificationRepository.findByUserIdAndIsRead(user.getId(), false, pageable);
        } else {
            pageResult = notificationRepository.findByUserId(user.getId(), pageable);
        }

        List<NotificationResponse> content = pageResult.getContent().stream()
                .map(notificationMapper::toResponse).toList();
        return PageResponse.of(pageResult, content);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String currentUserEmail) {
        User user = getUser(currentUserEmail);
        return notificationRepository.countByUserIdAndIsRead(user.getId(), false);
    }

    @Override
    @Transactional
    public void markRead(Long notificationId, String currentUserEmail) {
        User user = getUser(currentUserEmail);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BusinessRuleException("You can only mark your own notifications as read");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllRead(String currentUserEmail) {
        User user = getUser(currentUserEmail);
        int updated = notificationRepository.markAllReadByUserId(user.getId());
        log.debug("Marked {} notifications as read for user {}", updated, currentUserEmail);
    }

    @Override
    @Transactional
    public Notification createNotification(User user, NotificationType type, String title, String message,
                                            String refEntityType, Long refEntityId) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .refEntityType(refEntityType)
                .refEntityId(refEntityId)
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
