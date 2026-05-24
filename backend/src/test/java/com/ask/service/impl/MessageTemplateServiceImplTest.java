package com.ask.service.impl;

import com.ask.dto.request.message.MessageTemplateRequest;
import com.ask.dto.response.message.MessageTemplateResponse;
import com.ask.entity.MessageTemplate;
import com.ask.enums.MessageChannel;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.repository.MessageTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageTemplateServiceImplTest {

    @Mock
    private MessageTemplateRepository templateRepository;

    @InjectMocks
    private MessageTemplateServiceImpl templateService;

    @Test
    void createTemplateSavesSuccessfully() {
        MessageTemplateRequest request = MessageTemplateRequest.builder()
                .name("Welcome Template")
                .channel(MessageChannel.EMAIL)
                .content("Hello {{name}}")
                .build();

        when(templateRepository.findByName(request.getName())).thenReturn(Optional.empty());
        when(templateRepository.save(any(MessageTemplate.class))).thenAnswer(i -> {
            MessageTemplate t = i.getArgument(0);
            t.setId(1L);
            return t;
        });

        MessageTemplateResponse response = templateService.createTemplate(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Welcome Template", response.getName());
        verify(templateRepository).save(any(MessageTemplate.class));
    }

    @Test
    void createTemplateThrowsIfDuplicateName() {
        MessageTemplateRequest request = MessageTemplateRequest.builder()
                .name("Welcome Template")
                .build();

        when(templateRepository.findByName(request.getName())).thenReturn(Optional.of(new MessageTemplate()));

        assertThrows(BusinessRuleException.class, () ->
                templateService.createTemplate(request)
        );
        verify(templateRepository, never()).save(any());
    }

    @Test
    void getTemplateThrowsIfNotFound() {
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                templateService.getTemplate(99L)
        );
    }
}
