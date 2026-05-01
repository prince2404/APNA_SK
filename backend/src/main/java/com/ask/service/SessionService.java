package com.ask.service;

import com.ask.dto.response.auth.UserSessionResponse;
import com.ask.dto.response.common.PageResponse;

/**
 * Service for viewing and revoking the authenticated user's sessions.
 */
public interface SessionService {

    PageResponse<UserSessionResponse> getCurrentUserSessions(String email, int page, int size);

    void revokeSession(String email, Long sessionId);
}
