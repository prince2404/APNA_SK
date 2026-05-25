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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.NOTIFICATIONS)
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_NOTIFICATIONS_VIEW_NOTIFICATIONS')")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<NotificationResponse> response = notificationService.getNotifications(
                userDetails.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.NOTIFICATIONS));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_NOTIFICATIONS_VIEW_NOTIFICATIONS')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = notificationService.getUnreadCount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(count, ApiPaths.NOTIFICATIONS + "/unread-count"));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_NOTIFICATIONS_VIEW_NOTIFICATIONS')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        notificationService.markAsRead(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read",
                ApiPaths.NOTIFICATIONS + "/" + id + "/read"));
    }

    @PatchMapping("/read-all")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_NOTIFICATIONS_VIEW_NOTIFICATIONS')")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read",
                ApiPaths.NOTIFICATIONS + "/read-all"));
    }
}
