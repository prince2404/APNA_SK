package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.response.auth.UserSessionResponse;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for authenticated users to view and revoke their active sessions.
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.SESSIONS)
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<UserSessionResponse>>> getSessions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                sessionService.getCurrentUserSessions(userDetails.getUsername(), page, size),
                ApiPaths.SESSIONS));
    }

    @PatchMapping("/{id}/revoke")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        sessionService.revokeSession(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Session revoked",
                ApiPaths.SESSIONS + "/" + id + "/revoke"));
    }
}
