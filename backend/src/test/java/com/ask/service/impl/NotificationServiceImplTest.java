package com.ask.service.impl;

import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.notification.NotificationResponse;
import com.ask.entity.Notification;
import com.ask.entity.User;
import com.ask.enums.NotificationType;
import com.ask.exception.ResourceNotFoundException;
import com.ask.repository.NotificationRepository;
import com.ask.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void getNotificationsReturnsPageOfNotifications() {
        User user = testUser(100L, "user@askhealth.in");
        Notification notification = Notification.builder()
                .id(1L)
                .user(user)
                .type(NotificationType.KYC_SUBMISSION)
                .title("Title")
                .message("Message")
                .isRead(false)
                .build();
        Page<Notification> page = new PageImpl<>(List.of(notification));

        when(userRepository.findByEmail("user@askhealth.in")).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserId(eq(100L), any(Pageable.class))).thenReturn(page);

        PageResponse<NotificationResponse> result = notificationService.getNotifications("user@askhealth.in", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Title", result.getContent().get(0).getTitle());
    }

    @Test
    void getUnreadCountReturnsCount() {
        User user = testUser(100L, "user@askhealth.in");

        when(userRepository.findByEmail("user@askhealth.in")).thenReturn(Optional.of(user));
        when(notificationRepository.countByUserIdAndIsReadFalse(100L)).thenReturn(5L);

        long count = notificationService.getUnreadCount("user@askhealth.in");

        assertEquals(5L, count);
    }

    @Test
    void markAsReadUpdatesNotification() {
        User user = testUser(100L, "user@askhealth.in");
        Notification notification = Notification.builder()
                .id(1L)
                .user(user)
                .isRead(false)
                .build();

        when(userRepository.findByEmail("user@askhealth.in")).thenReturn(Optional.of(user));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L, "user@askhealth.in");

        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsReadThrowsAccessDeniedForDifferentUser() {
        User user = testUser(100L, "user@askhealth.in");
        User otherUser = testUser(200L, "other@askhealth.in");
        Notification notification = Notification.builder()
                .id(1L)
                .user(otherUser)
                .isRead(false)
                .build();

        when(userRepository.findByEmail("user@askhealth.in")).thenReturn(Optional.of(user));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThrows(AccessDeniedException.class, () ->
                notificationService.markAsRead(1L, "user@askhealth.in")
        );
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllAsReadCallsRepository() {
        User user = testUser(100L, "user@askhealth.in");

        when(userRepository.findByEmail("user@askhealth.in")).thenReturn(Optional.of(user));

        notificationService.markAllAsRead("user@askhealth.in");

        verify(notificationRepository).markAllAsReadForUser(100L);
    }

    @Test
    void sendNotificationCreatesAndSavesNotification() {
        User recipient = testUser(100L, "user@askhealth.in");

        notificationService.sendNotification(recipient, NotificationType.KYC_SUBMISSION, "Title", "Msg", "USER", 100L);

        verify(notificationRepository).save(any(Notification.class));
    }

    private User testUser(Long id, String email) {
        return User.builder()
                .id(id)
                .fullName("Test User")
                .email(email)
                .build();
    }
}
