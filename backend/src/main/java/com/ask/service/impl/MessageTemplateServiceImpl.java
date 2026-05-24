package com.ask.service.impl;

import com.ask.dto.request.message.MessageTemplateRequest;
import com.ask.dto.response.message.MessageTemplateResponse;
import com.ask.entity.MessageTemplate;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.repository.MessageTemplateRepository;
import com.ask.service.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageTemplateServiceImpl implements MessageTemplateService {

    private final MessageTemplateRepository templateRepository;

    @Override
    @Transactional
    public MessageTemplateResponse createTemplate(MessageTemplateRequest request) {
        templateRepository.findByName(request.getName()).ifPresent(t -> {
            throw new BusinessRuleException("Message template with name '" + request.getName() + "' already exists");
        });

        MessageTemplate template = MessageTemplate.builder()
                .name(request.getName())
                .channel(request.getChannel())
                .content(request.getContent())
                .build();

        MessageTemplate saved = templateRepository.save(template);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public MessageTemplateResponse updateTemplate(Long id, MessageTemplateRequest request) {
        MessageTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MessageTemplate", "id", id));

        templateRepository.findByName(request.getName()).ifPresent(t -> {
            if (!t.getId().equals(id)) {
                throw new BusinessRuleException("Message template with name '" + request.getName() + "' already exists");
            }
        });

        template.setName(request.getName());
        template.setChannel(request.getChannel());
        template.setContent(request.getContent());

        MessageTemplate saved = templateRepository.save(template);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageTemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageTemplateResponse getTemplate(Long id) {
        MessageTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MessageTemplate", "id", id));
        return toResponse(template);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        MessageTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MessageTemplate", "id", id));
        templateRepository.delete(template);
    }

    private MessageTemplateResponse toResponse(MessageTemplate template) {
        return MessageTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .channel(template.getChannel())
                .content(template.getContent())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
