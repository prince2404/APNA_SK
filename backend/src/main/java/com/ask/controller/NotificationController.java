package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.notification.NotificationResponse;
import com.ask.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for in-app notification management.
 * All endpoints operate on the currently authenticated user's own notifications.
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.NOTIFICATIONS)
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Get paginated notifications for the current user. */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getMyNotifications(userDetails.getUsername(), unreadOnly, page, size),
                ApiPaths.NOTIFICATIONS));
    }

    /** Get the count of unread notifications. */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = notificationService.getUnreadCount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("unreadCount", count), ApiPaths.NOTIFICATIONS + "/unread-count"));
    }

    /** Mark a single notification as read. */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        notificationService.markRead(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read",
                ApiPaths.NOTIFICATIONS + "/" + id + "/read"));
    }

    /** Mark all notifications as read for the current user. */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllRead(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read",
                ApiPaths.NOTIFICATIONS + "/read-all"));
    }
}
