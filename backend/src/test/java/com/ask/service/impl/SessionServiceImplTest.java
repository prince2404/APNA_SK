package com.ask.service.impl;

import com.ask.entity.User;
import com.ask.entity.UserSession;
import com.ask.exception.ResourceNotFoundException;
import com.ask.repository.UserRepository;
import com.ask.repository.UserSessionRepository;
import com.ask.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSessionRepository userSessionRepository;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private SessionServiceImpl sessionService;

    @Test
    void revokeSessionRevokesOnlyCurrentUsersSessionAndAudits() {
        User user = User.builder().id(1L).email("user@askhealth.in").build();
        UserSession session = UserSession.builder()
                .id(7L)
                .user(user)
                .lastActiveAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .isRevoked(false)
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userSessionRepository.findByIdAndUserId(session.getId(), user.getId())).thenReturn(Optional.of(session));

        sessionService.revokeSession(user.getEmail(), session.getId());

        assertTrue(session.getIsRevoked());
        verify(userSessionRepository).save(session);
        verify(auditService).log(eq(user), eq("REVOKE_SESSION"), eq("USER_SESSION"),
                eq(session.getId()), eq(null), eq(null), eq(null), any());
    }

    @Test
    void revokeSessionRejectsSessionOutsideCurrentUser() {
        User user = User.builder().id(1L).email("user@askhealth.in").build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userSessionRepository.findByIdAndUserId(99L, user.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sessionService.revokeSession(user.getEmail(), 99L));
        verify(userSessionRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any(), any(), any(), any());
    }
}
