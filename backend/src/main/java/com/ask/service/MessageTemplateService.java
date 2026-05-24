package com.ask.service;

import com.ask.dto.request.message.MessageTemplateRequest;
import com.ask.dto.response.message.MessageTemplateResponse;

import java.util.List;

public interface MessageTemplateService {
    MessageTemplateResponse createTemplate(MessageTemplateRequest request);
    MessageTemplateResponse updateTemplate(Long id, MessageTemplateRequest request);
    List<MessageTemplateResponse> getAllTemplates();
    MessageTemplateResponse getTemplate(Long id);
    void deleteTemplate(Long id);
}
