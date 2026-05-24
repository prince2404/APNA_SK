package com.ask.service.impl;

import com.ask.dto.request.message.BulkMessageRequest;
import com.ask.dto.response.message.BulkMessageLogResponse;
import com.ask.entity.*;
import com.ask.enums.*;
import com.ask.repository.BulkMessageLogRepository;
import com.ask.repository.MessageTemplateRepository;
import com.ask.repository.UserRepository;
import com.ask.service.EmailService;
import com.ask.service.SmsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkMessageServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MessageTemplateRepository templateRepository;
    @Mock
    private BulkMessageLogRepository logRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private SmsService smsService;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private BulkMessageServiceImpl bulkMessageService;

    private User user(Long id, String email, String roleName) {
        Role role = Role.builder().id(id).name(roleName).displayName(roleName).build();
        return User.builder()
                .id(id)
                .fullName("Test User " + id)
                .email(email)
                .role(role)
                .build();
    }

    @Test
    void sendBulkMessageThrowsAccessDeniedForNonAdmin() {
        User receptionist = user(2L, "receptionist@askhealth.in", "RECEPTIONIST");
        when(userRepository.findByEmail(receptionist.getEmail())).thenReturn(Optional.of(receptionist));

        BulkMessageRequest request = BulkMessageRequest.builder()
                .channel(MessageChannel.EMAIL)
                .build();

        assertThrows(AccessDeniedException.class, () ->
                bulkMessageService.sendBulkMessage(request, receptionist.getEmail())
        );
    }

    @Test
    void sendBulkMessageResolvesTargetsAndDispatchesEmails() {
        User superAdmin = user(1L, "admin@askhealth.in", "SUPER_ADMIN");
        when(userRepository.findByEmail(superAdmin.getEmail())).thenReturn(Optional.of(superAdmin));

        User targetUser = user(10L, "target@askhealth.in", "RECEPTIONIST");
        targetUser.setFullName("Patna Receptionist");

        TypedQuery<User> queryMock = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(queryMock);
        when(queryMock.getResultList()).thenReturn(List.of(targetUser));

        when(logRepository.save(any(BulkMessageLog.class))).thenAnswer(i -> {
            BulkMessageLog log = i.getArgument(0);
            log.setId(100L);
            return log;
        });

        BulkMessageRequest request = BulkMessageRequest.builder()
                .customText("Hello {{name}}, welcome to {{role}}!")
                .channel(MessageChannel.EMAIL)
                .targetRole("RECEPTIONIST")
                .build();

        BulkMessageLogResponse response = bulkMessageService.sendBulkMessage(request, superAdmin.getEmail());

        assertNotNull(response);
        assertEquals(1, response.getSentCount());
        assertEquals(BulkMessageStatus.SUCCESS, response.getStatus());
        verify(emailService).sendSimpleEmail(eq("target@askhealth.in"), anyString(), eq("Hello Patna Receptionist, welcome to RECEPTIONIST!"));
        verify(logRepository).save(any(BulkMessageLog.class));
    }
}
