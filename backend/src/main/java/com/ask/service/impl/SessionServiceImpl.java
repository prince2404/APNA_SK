package com.ask.service.impl;

import com.ask.constants.AppConstants;
import com.ask.dto.response.auth.UserSessionResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.entity.User;
import com.ask.entity.UserSession;
import com.ask.exception.ResourceNotFoundException;
import com.ask.repository.UserRepository;
import com.ask.repository.UserSessionRepository;
import com.ask.service.AuditService;
import com.ask.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Session management implementation. Sessions are revoked, never deleted.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserSessionResponse> getCurrentUserSessions(String email, int page, int size) {
        User user = getUser(email);
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Page<UserSession> sessionPage = userSessionRepository.findByUserIdAndIsRevokedFalse(
                user.getId(), PageRequest.of(page, size, Sort.by("lastActiveAt").descending()));
        List<UserSessionResponse> content = sessionPage.getContent().stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(sessionPage, content);
    }

    @Override
    @Transactional
    public void revokeSession(String email, Long sessionId) {
        User user = getUser(email);
        UserSession session = userSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Session", "id", sessionId));
        session.setIsRevoked(true);
        userSessionRepository.save(session);
        auditService.log(user, "REVOKE_SESSION", "USER_SESSION", session.getId(), null, null, null,
                "Revoked user session");
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private UserSessionResponse toResponse(UserSession session) {
        return UserSessionResponse.builder()
                .id(session.getId())
                .deviceInfo(session.getDeviceInfo())
                .ipAddress(session.getIpAddress())
                .lastActiveAt(session.getLastActiveAt())
                .expiresAt(session.getExpiresAt())
                .isRevoked(session.getIsRevoked())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
